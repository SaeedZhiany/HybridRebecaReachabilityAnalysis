package stateSpace;

import com.rits.cloning.Cloner;
import dataStructure.ContinuousVariable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.rebecalang.compiler.modelcompiler.corerebeca.objectmodel.*;
import org.rebecalang.compiler.modelcompiler.timedrebeca.objectmodel.TimedRebecaParentSuffixPrimary;
import utils.CompilerUtil;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class HybridStateTest {
    private HybridState hybridState;
//    @Mock
    private CompilerUtil compilerUtilMock;
//    @Mock
    private RebecInstantiationMapping rebecInstantiationMappingMock;
//    @Mock
    private  ParentSuffixPrimary parentSuffixPrimaryMock;

    ContinuousVariable createContinuousVariable(BigDecimal lowerBound, BigDecimal upperBound) {
        return new ContinuousVariable("continuousVariable", lowerBound, upperBound);
    }
    @BeforeEach
    void setUp() {
        compilerUtilMock = mock(CompilerUtil.class);
        rebecInstantiationMappingMock = mock(RebecInstantiationMapping.class);
        parentSuffixPrimaryMock = mock(ParentSuffixPrimary.class);
    }

    @Test
    @Tag("test sendStatement with one parameter")
    void testSendStatementWithOneParameter() {
        try (MockedStatic<RebecInstantiationMapping> rebecInstantiationMappingMockedStatic = mockStatic(RebecInstantiationMapping.class);
             MockedStatic<CompilerUtil> compilerUtilMockedStatic = mockStatic(CompilerUtil.class)) {
            rebecInstantiationMappingMockedStatic.when(RebecInstantiationMapping::getInstance).thenReturn(rebecInstantiationMappingMock);
            DotPrimary statement = new DotPrimary();
            TermPrimary right = new TermPrimary();
            right.setName("messageServerName");
            TermPrimary left = new TermPrimary();
            left.setName("knownRebecName");
            statement.setRight(right);
            statement.setLeft(left);
            List<Statement> sigma = new ArrayList<>();
            sigma.add(statement);

            PhysicalState physicalState1 = new PhysicalState("physicalState1", "none", new HashMap<>(), new HashSet<>(), sigma, 0);
            PhysicalState physicalState2 = new PhysicalState("physicalState2", "none", new HashMap<>(), new HashSet<>(), new ArrayList<>(), 0);
            HashMap<String, PhysicalState> physicalStateHashMap = new HashMap<>();
            physicalStateHashMap.put(physicalState1.getActorName(), physicalState1);
            physicalStateHashMap.put(physicalState2.getActorName(), physicalState2);
            int messageArrivalLowerBound = 0;
            int messageArrivalUpperBound = 1;
            ContinuousVariable continuousVariable = createContinuousVariable(
                    new BigDecimal(messageArrivalLowerBound),
                    new BigDecimal(messageArrivalUpperBound));
            hybridState = new HybridState(continuousVariable, new HashMap<>(), physicalStateHashMap, new CANNetworkState());
            when(rebecInstantiationMappingMock.getKnownRebecBinding(physicalState1.getActorName(), left.getName())).thenReturn(physicalState2.getActorName());
            String serverName = right.getName();

            List<FormalParameterDeclaration> formalParameterDeclarations = new ArrayList<>();
            formalParameterDeclarations.add(new FormalParameterDeclaration());
            formalParameterDeclarations.get(0).setName("formalParameterName1");
            when(rebecInstantiationMappingMock.getRebecReactiveClassType(physicalState2.getActorName())).thenReturn("ReactiveClassType");

            compilerUtilMockedStatic.when(() -> CompilerUtil.getServerParameters("ReactiveClassType", "messageServerName")).thenReturn(formalParameterDeclarations);
            Literal literal = new Literal();
            literal.setLiteralValue("2");
            OrdinaryPrimitiveType ordinaryPrimitiveType = new OrdinaryPrimitiveType();
            ordinaryPrimitiveType.setName("int");
            literal.setType(ordinaryPrimitiveType);
            ArrayList<Expression> parentSuffixPrimaryArguments = new ArrayList<>();
            parentSuffixPrimaryArguments.add(literal);
            when(parentSuffixPrimaryMock.getArguments()).thenReturn(parentSuffixPrimaryArguments);
            right.setParentSuffixPrimary(parentSuffixPrimaryMock);
            Cloner cloner = new Cloner();
            HybridState newHybridState1 = cloner.deepClone(hybridState);

            HybridState newHybridState = hybridState.sendStatement(physicalState1, newHybridState1);
            assertEquals(physicalState2.getMessageBag().size(), 1);

        }
    }

}