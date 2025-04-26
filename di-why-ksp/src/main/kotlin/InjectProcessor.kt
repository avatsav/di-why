import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.isAnnotationPresent
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSVisitorVoid
import com.google.devtools.ksp.validate
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.ksp.writeTo
import dev.avatsav.diwhy.Factory
import dev.avatsav.diwhy.Inject
import dev.avatsav.diwhy.ObjectGraph
import dev.avatsav.diwhy.Singleton

class InjectProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return InjectProcessor(environment.codeGenerator, environment.logger)
    }
}

/**
 * Processes @Inject annotated constructors during compilation to generate factory classes.
 *
 * The processor creates two types of factories:
 *
 * 1. Standard factories for regular classes:
 * ```
 * class CoffeeMachine_Factory : Factory<CoffeeMachine> {
 *     override fun get(objectGraph: ObjectGraph): CoffeeMachine {
 *         return CoffeeMachine(
 *             logger = objectGraph.get(),
 *             heater = objectGraph.get(),
 *             pump = objectGraph.get()
 *         )
 *     }
 * }
 * ```
 *
 * 2. Singleton factories for @Singleton annotated classes:
 * ```
 * class CoffeeMachineLogger_Factory : Factory<CoffeeMachineLogger> {
 *     private val singletonFactory = singleton { objectGraph -> CoffeeMachineLogger() }
 *     override fun get(objectGraph: ObjectGraph) = singletonFactory.get(objectGraph)
 * }
 * ```
 */
class InjectProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
) : SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val annotatedSymbols = resolver.getSymbolsWithAnnotation(Inject::class.java.name)
        val unprocessedSymbols = annotatedSymbols.filterNot { it.validate() }.toList()
        annotatedSymbols
            .filter { it is KSFunctionDeclaration && it.validate() }
            .forEach { it.accept(InjectConstructorVisitor(), Unit) }
        return unprocessedSymbols
    }

    private companion object {
        private val factoryClassName = Factory::class.asClassName()
        private val objectGraphClassName = ObjectGraph::class.asClassName()
        private val singletonMemberName = MemberName("dev.avatsav.diwhy", "singleton")
        private val getMemberName = MemberName("dev.avatsav.diwhy", "get", true)
        private const val generatedPackageName = "dev.a vatsav.diwhy.generated"
    }

    inner class InjectConstructorVisitor : KSVisitorVoid() {
        @OptIn(KspExperimental::class)
        override fun visitFunctionDeclaration(function: KSFunctionDeclaration, data: Unit) {
            val injectedClass = function.parentDeclaration as KSClassDeclaration
            val injectedClassSimpleName = injectedClass.simpleName.asString()
            val className = "${injectedClassSimpleName}_Factory"

            val parameterizedFactoryClassName = factoryClassName.parameterizedBy(injectedClass.toClassName())

            val constructorParams = getConstructorParameters(injectedClass)
            val constructorParamsCodeBlock = CodeBlock.builder()
            if (constructorParams.isNotEmpty()) {
                constructorParams.forEachIndexed { index, param ->
                    constructorParamsCodeBlock.add("${param.name} = objectGraph.%M()", getMemberName)
                    if (index < constructorParams.size - 1) {
                        constructorParamsCodeBlock.add(", ")
                    }
                }
            }

            val createInstanceCodeBlock = CodeBlock.builder()
                .add("%T(", injectedClass.toClassName())
                .add(constructorParamsCodeBlock.build())
                .add(")")
                .build()

            val factoryBuilder = TypeSpec.classBuilder(className)
                .addSuperinterface(parameterizedFactoryClassName)

            val functionBuilder = FunSpec.builder("get")
                .addModifiers(KModifier.OVERRIDE)
                .addParameter("objectGraph", objectGraphClassName)
                .returns(injectedClass.toClassName())

            if (injectedClass.isAnnotationPresent(Singleton::class)) {
                val initializerCodeBlock = CodeBlock.builder()
                    .add("%M { objectGraph -> ", singletonMemberName)
                    .add(createInstanceCodeBlock)
                    .add(" }")
                    .build()

                factoryBuilder.addProperty(
                    PropertySpec.builder("singletonFactory", parameterizedFactoryClassName, KModifier.PRIVATE)
                        .initializer(initializerCodeBlock)
                        .build()
                )
                functionBuilder.addStatement("return singletonFactory.get(objectGraph)")
            } else {
                functionBuilder.addStatement("return %L", createInstanceCodeBlock)
            }

            // Build file
            val fileSpec = FileSpec.builder(generatedPackageName, className)
                .addType(
                    factoryBuilder
                        .addFunction(functionBuilder.build())
                        .build()
                )
                .build()

            fileSpec.writeTo(codeGenerator, aggregating = false)
        }

        /**
         * Gets the constructor parameters for a class declaration.
         *
         * @param element The class declaration to get constructor parameters for
         * @return A list of ParameterSpec objects representing the constructor parameters
         */
        private fun getConstructorParameters(element: KSClassDeclaration): List<ParameterSpec> {
            val primaryConstructor = element.primaryConstructor ?: return emptyList()

            // Pre-allocate the list with the exact size needed
            val parameters = primaryConstructor.parameters
            if (parameters.isEmpty()) return emptyList()

            return parameters.map { param ->
                val paramName = param.name?.asString() ?: "unnamed"
                ParameterSpec.builder(paramName, param.type.toTypeName()).build()
            }
        }
    }
}
