package popfunc.beast.evolution.populationmodel;

import beast.base.evolution.tree.Tree;
import beast.base.evolution.operator.kernel.AdaptableVarianceMultivariateNormalOperator;

public interface PopFuncWithAVMNOp {

    AdaptableVarianceMultivariateNormalOperator getAVMNOperator(Tree tree);
}
