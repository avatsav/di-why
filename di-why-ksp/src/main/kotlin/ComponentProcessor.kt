import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.KSAnnotated

class ComponentProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor =
        ComponentProcessor(environment.codeGenerator, environment.logger)
}

class ComponentProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
) : SymbolProcessor {
    override fun process(resolver: Resolver): List<KSAnnotated> {
        logger.warn("In the component  processor!")
        return emptyList()
    }
}
