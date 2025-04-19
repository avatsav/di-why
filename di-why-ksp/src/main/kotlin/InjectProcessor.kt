import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.KSAnnotated

class InjectProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return InjectProcessor(environment.codeGenerator, environment.logger)
    }
}

class InjectProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
) : SymbolProcessor {
    override fun process(resolver: Resolver): List<KSAnnotated> {
        logger.warn("In the inject processor")
        return emptyList()
    }

}