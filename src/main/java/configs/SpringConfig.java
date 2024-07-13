package configs;

import org.rebecalang.compiler.modelcompiler.RebecaModelCompiler;
import org.rebecalang.compiler.modelcompiler.SymbolTable;
import org.rebecalang.compiler.utils.ExceptionContainer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.rebecalang.compiler.modelcompiler.corerebeca.CoreRebecaCompleteCompilerFacade;
@Configuration
@ComponentScan(basePackages = {"org.rebecalang.compiler", "utils"})
public class SpringConfig {

//    @Bean
//    public RebecaModelCompiler rebecaModelCompiler() {
//        return new RebecaModelCompiler();
//    }
//
//    @Bean
//    public ExceptionContainer exceptionContainer() {
//        return new ExceptionContainer();
//    }
//
//    @Bean
//    public SymbolTable symbolTable() {
//        return new SymbolTable();
//    }
//
//    @Bean
//    public CoreRebecaCompleteCompilerFacade coreRebecaCompleteCompilerFacade() {
//        return new CoreRebecaCompleteCompilerFacade();
//    }
}
