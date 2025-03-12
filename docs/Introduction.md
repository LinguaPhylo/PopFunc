# Introduction

## Background and Motivation

In evolutionary biology, epidemiology, and cancer genomics, **coalescent-based phylodynamic models** play a crucial role in reconstructing population size histories from genetic data. Traditional coalescent frameworks often assume a single parametric function—such as constant or exponential growth—which may be too restrictive for real-world systems that experience more complex demographic changes (e.g., logistic or Gompertz-like dynamics).

**PopFunc** extends the **BEAST2** platform by:
1. Incorporating a range of **parametric population-growth models** (e.g., exponential, logistic, Gompertz), along with their “expansion” variants that allow for a nonzero ancestral population size.
2. Providing a **Bayesian Model Averaging (BMA)** mechanism to seamlessly switch among different models via discrete indicator parameters. This reduces the risk of model misspecification and allows the data to inform the best-fitting demographic trajectory.

## Key Features

- **Unified Model Space**  
  All candidate demographic functions (constant, exponential, logistic, Gompertz, etc.) can be sampled in a single Bayesian inference run.

- **Discrete Indicators**  
  Each model is associated with an indicator variable, enabling the sampler to move between different demographic assumptions without restarting or running separate analyses.

- **Metropolis-Coupled MCMC**  
  To address mixing challenges—especially for more complex models—PopFunc integrates with **MC³**.

- **Adaptive Operators**  
  Optional support for specialized MCMC operators (Up-Down, AVMN) facilitates handling correlations among parameters (e.g., in Gompertz models).

## Typical Use Cases

- **Viral Epidemiology**  
  Reconstructing rapid pathogen expansions (e.g., HCV) from molecular surveillance data.
- **Cancer Genomics**  
  Inferring tumor growth trajectories (e.g., Gompertz expansions) from single-cell sequencing data.
- **Population Genetics**  
  Comparing multiple growth hypotheses (constant, logistic, etc.) in a single unified Bayesian framework.

For more details on how these models are parameterized and applied, refer to our accompanying paper and the [Usage & BMA Setup Guide](/Users/xuyuan/workspace/PopFunc/docs/Bayesian_Model_Averaging_Setup.md).