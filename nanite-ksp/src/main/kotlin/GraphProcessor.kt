import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getConstructors
import com.google.devtools.ksp.getDeclaredProperties
import com.google.devtools.ksp.isAnnotationPresent
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSVisitorVoid
import com.google.devtools.ksp.validate
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toKModifier
import com.squareup.kotlinpoet.ksp.writeTo
import dev.avatsav.diwhy.Binds
import dev.avatsav.diwhy.Graph
import dev.avatsav.diwhy.Inject
import dev.avatsav.diwhy.Singleton
import kotlin.reflect.KClass

class DependencyGraphProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor =
        DependencyGraphProcessor(environment.codeGenerator, environment.logger)
}

@OptIn(KspExperimental::class)
class DependencyGraphProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
) : SymbolProcessor {
    override fun process(resolver: Resolver): List<KSAnnotated> {
        val annotatedSymbols = resolver.getSymbolsWithAnnotation(Graph::class.java.name)
        val unprocessedSymbols = annotatedSymbols.filterNot { it.validate() }.toList()
        annotatedSymbols.filter { it is KSClassDeclaration && it.validate() && it.classKind == ClassKind.INTERFACE }
            .forEach { it.accept(DependencyGraphVisitor(), Unit) }
        return unprocessedSymbols
    }

    private companion object {
        val graphSingletonMember = MemberName("dev.avatsav.diwhy", "graphSingleton")
    }

    inner class DependencyGraphVisitor : KSVisitorVoid() {

        override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
            val interfaceName = classDeclaration.simpleName.asString()
            val packageName = classDeclaration.containingFile!!.packageName.asString()
            val className = "Generated${interfaceName}"
            val graphAnnotation = classDeclaration.annotations.single { it isInstance Graph::class }

            val entryPoints = readEntryPoints(classDeclaration)
            val binds = readBinds(graphAnnotation)

            binds.forEach { (requested, provided) ->
                logger.warn("Binding: $requested -> $provided")
            }
            val factoriesToBuild = entryPoints.map { it.propertyType }.toSet() + binds.values.toSet()
            val factories = traverseDependencyGraph(factoriesToBuild)
            val model = GraphGenModel(packageName, className, classDeclaration, entryPoints, factories)
            generateGraph(codeGenerator, model)
        }

        private fun generateGraph(
            codeGenerator: CodeGenerator,
            model: GraphGenModel,
        ) = with(model) {
            val fileSpec = FileSpec.builder(packageName, className)
            val typeSpec = TypeSpec.classBuilder(className)
                .addSuperinterface(graphInterface.toClassName())

            factories.forEach { factory ->
                typeSpec.addProperty(factory.createPropertyProviderFactoryCodeBlock())
            }
            entryPoints.forEach { entryPoint ->
                typeSpec.addProperty(entryPoint.createPropertyProviderFactoryCodeBlock())
            }
            fileSpec.addType(typeSpec.build())
            fileSpec.build().writeTo(codeGenerator, false)
        }

        private fun EntryPoint.createPropertyProviderFactoryCodeBlock(): PropertySpec {
            return PropertySpec.builder(
                name = property.simpleName.asString(),
                type = property.type.resolve().toClassName(),
                modifiers = property.modifiers.mapNotNull { it.toKModifier() } + KModifier.OVERRIDE,
            ).initializer(" { provide${property.simpleName.asString()}() }")
                .build()
        }

        private fun GraphFactory.createPropertyProviderFactoryCodeBlock(): PropertySpec {
            val constructorParamsCodeBlock = CodeBlock.builder()
            if (constructorParameters.isNotEmpty()) {
                constructorParameters.forEachIndexed { index, param ->
                    constructorParamsCodeBlock.add("provide${param.simpleName.asString()}()")
                    if (index < constructorParameters.size - 1) {
                        constructorParamsCodeBlock.add(", ")
                    }
                }
            }
            val createInstanceCodeBlock = CodeBlock.builder()
                .add("%T(", type.toClassName())
                .add(constructorParamsCodeBlock.build())
                .add(")")
                .build()


            val factoryProperty = PropertySpec.builder(
                "provide" + type.simpleName.asString(), type.toClassName()
            )
                .addModifiers(KModifier.PRIVATE)
                .initializer(
                    CodeBlock.builder()
                        .addStatement(if (singleton) " { " else "%M { ", graphSingletonMember)
                        .add(createInstanceCodeBlock)
                        .add("}")
                        .build()
                ).build()
            return factoryProperty
        }


        private fun readEntryPoints(classDeclaration: KSClassDeclaration): List<EntryPoint> =
            classDeclaration.getDeclaredProperties().map { property ->
                val declaration = property.type.resolve().declaration
                EntryPoint(property, declaration)
            }.toList()

        @Suppress("UNCHECKED_CAST")
        private fun readBinds(graphAnnotation: KSAnnotation): Map<KSDeclaration, KSDeclaration> {
            val modules = graphAnnotation.arguments.single { it.name?.asString() == "modules" }.value as List<KSType>
            return modules
                .map { it.declaration as KSClassDeclaration }
                .flatMap { it.annotations }
                .filter { it isInstance Binds::class }
                .associate { annotation ->
                    val args = annotation.annotationType.resolve().arguments
                    val requested = args.first().type!!.resolve().declaration
                    val provided = args.last().type!!.resolve().declaration
                    requested to provided
                }
        }

        private fun traverseDependencyGraph(factoriesToBuild: Set<KSDeclaration>): List<GraphFactory> {
            val typesToProcess = factoriesToBuild.toMutableList()
            val factories = mutableListOf<GraphFactory>()
            val visitedTypes = mutableSetOf<KSDeclaration>()
            while (typesToProcess.isNotEmpty()) {
                val type = typesToProcess.removeFirst() as KSClassDeclaration
                if (type !in visitedTypes) {
                    visitedTypes += type
                    val injectConstructor = type.getConstructors()
                        .filter { it.isAnnotationPresent(Inject::class) }
                        .toList()
                    check(injectConstructor.size < 2) { "There should be at-most one @Inject constructor: ${type.simpleName.asString()}" }
                    if (injectConstructor.isNotEmpty()) {
                        val constructor = injectConstructor.first()
                        val constructorParameters = constructor.parameters.map { it.type.resolve().declaration }
                        typesToProcess += constructorParameters
                        val singleton = type.isAnnotationPresent(Singleton::class)
                        factories += GraphFactory(type, constructorParameters, singleton)
                    }
                }
            }
            return factories
        }
    }
}

data class EntryPoint(
    val property: KSPropertyDeclaration,
    val propertyType: KSDeclaration,
)

data class GraphGenModel(
    val packageName: String,
    val className: String,
    val graphInterface: KSClassDeclaration,
    val entryPoints: List<EntryPoint>,
    val factories: List<GraphFactory>,
)

data class GraphFactory(
    val type: KSClassDeclaration,
    val constructorParameters: List<KSDeclaration>,
    val singleton: Boolean
)

infix fun KSAnnotation.isInstance(annotationKClass: KClass<*>): Boolean {
    return shortName.getShortName() == annotationKClass.simpleName &&
            annotationType.resolve().declaration.qualifiedName?.asString() == annotationKClass.qualifiedName
}
