package popfunc.lphybeast.spi;

import beast.base.evolution.datatype.DataType;
import jebl.evolution.sequences.SequenceType;
import lphy.base.distribution.UniformDiscrete;
import lphy.base.evolution.coalescent.PopulationFunctionCoalescent;
import lphy.base.evolution.coalescent.populationmodel.*;
import lphy.base.evolution.tree.*;
import lphy.base.function.Difference;
import lphy.base.function.Union;
import lphy.base.function.io.ReadTrees;
import lphy.core.model.Generator;
import lphybeast.GeneratorToBEAST;
import lphybeast.ValueToBEAST;
import lphybeast.spi.LPhyBEASTExt;

import popfunc.lphybeast.tobeast.generators.*;
import popfunc.lphybeast.tobeast.values.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The "Container" provider class of SPI
 * which include a list of {@link ValueToBEAST},
 * {@link GeneratorToBEAST}, and {@link DataType}
 * to extend.
 * @author Walter Xie
 */
public class LBPhylonco implements LPhyBEASTExt {

    @Override
    public List<Class<? extends ValueToBEAST>> getValuesToBEASTs() {
        return Arrays.asList(
                //PopulationFunctionToBEAST.class // TODO
                Gompertz_f0ToBEAST.class ,
                ExponentialToBEAST.class,
                LogisticToBEAST.class,
                Gompertz_t50ToBEAST.class,
                ExpansionToBEAST.class,
                ConstantToBEAST.class,
                Cons_Exp_ConsToBEAST.class,
                SVSPopulationFunctionToBEAST.class,
                ExpansionToBEAST.class

        );
    }

    @Override
    public List<Class<? extends GeneratorToBEAST>> getGeneratorToBEASTs() {
        return Arrays.asList(
                PopFuncCoalescentToBEAST.class
        );
    }

    @Override
    public Map<SequenceType, DataType> getDataTypeMap() {
        return new ConcurrentHashMap<>();
    }


    @Override
    public List<Class<? extends Generator>> getExcludedGenerator() {
        return Arrays.asList(

                PopulationFunctionCoalescent.class,
                SVSPopulationFunction.class,
                GompertzPopulationFunction_f0.class,
                GompertzPopulationFunction_t50.class,
                ExponentialPopulationFunction.class,
                LogisticPopulationFunction.class,
                ExpansionPopulationFunction.class,
                ConstantPopulationFunction.class,
                Cons_Exp_ConsPopulationFunction.class,
                UniformDiscrete.class);
    }

    @Override
    public List<Class> getExcludedValueType() {
        return Arrays.asList(TimeTreeNode.class,
                SVSPopulation.class);
    }

}
