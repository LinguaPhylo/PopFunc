# BEAST PopFunc

**BEAST PopFunc** is a package for the **BEAST2** that enables Bayesian inference of parametric population-growth models under a flexible model-averaging framework. By using discrete indicator variables, PopFunc allows users to explore multiple candidate demographic models in a single run without requiring prior commitment to one specific growth function.

## Citation

If you use **PopFunc** in your research, please cite:

> **Bayesian model-averaging of parametric coalescent models for phylodynamic inference**  
> _Yuan Xu, Kylie Chen, Walter Xie, Alexei J. Drummond, et al._



See ([PopFunc-paper](https://github.com/yxu927/PopFunc-paper)) for datasets 
and analyses mentioned in the paper.

If you have any questions, please use the 
[beast-users Google Group](https://groups.google.com/u/0/g/beast-users).

---

  ## Requirements

- **Java 17** 
- [BEAST v2.7](https://github.com/CompEvol/beast2)

For additional compatibility details (e.g., with `lphy` or `lphybeast`), please see the [latest release notes](https://github.com/YourUserName/PopFunc/releases).

## Features

- **Multiple parametric population-growth models**  
  Includes constant, exponential, logistic, Gompertz, and their “expansion” variants that account for nonzero ancestral sizes.
  
- **Bayesian Model Averaging (BMA)**  
  Uses discrete indicators to switch among different growth functions (and whether to include \(N_A>0\)) during MCMC. Posterior probabilities reflect how well each model fits the data.

- **Flexible integration**  
  Allowing use alongside standard or custom substitution models, clock models, and priors.


## Installation

### 1. Via the BEAST Package Manager (Recommended)

1. Launch **BEAUti**.
2. From the menu, select: **`File -> Manage Packages...`**
3. In the **Package Manager** window, locate `PopFunc` and click **Install/Upgrade**.
4. After the installation finishes, restart BEAUti. The `PopFunc` package should now show as “Installed.”

 ### 2. Via the Command Line

In your BEAST2 installation directory (e.g., `~/beast/bin` on Linux/Mac), run:

```bash
./packagemanager -add PopFunc
```

Once installed, PopFunc should appear in the `BEAST2` package list.

## Bayesian Model Averaging (BMA) Setup

You can configure the relevant parameters in either the BEAUti interface or by editing the XML file directly, allowing the MCMC to sample from the discrete indicators `I` (model choice) and `I_na` (whether an ancestral population size is included). If you wish to enable Metropolis-coupled MCMC (MC\(^3\)) for improved mixing, set it in the XML. After adjusting these settings, save and export your final `.xml` file.

For more information on Metropolis-coupled MCMC (MC\(^3\)), please refer to the [BEAST2 Metropolis-coupled MCMC](https://www.beast2.org/2020/01/14/metropolis-coupled-mcmcmc3-works.html).


## How to set up BEAST2 analysis to run with coupled MCMC

Make sure the CoupledMCMC package is [installed](https://www.beast2.org/managing-packages/).

### By editing an XML file in a text editor
In order to  set up a pre-prepared xml to run with coupled MCMC, open the `*.xml` and change the MCMC line in the xml.

To do so, go to the line with:

```xml
<run id="mcmc" spec="MCMC" chainLength="....." numInitializationAttempts="....">
```
To have a run with coupled MCMC, we have to replace that one line with:

```xml
 <run id="mcmcmc" spec="coupledMCMC.CoupledMCMC" chainLength="20000000" chains="4" deltaTemperature="0.15" resampleEvery="1000" target="0.234">
```
## Example Operators for Logistic/Gompertz Models
When inferring **logistic** or **Gompertz** population growth, adding specialized MCMC operators often improves sampling efficiency—particularly if parameters (e.g., `f0`, `b`, and tree height) are strongly correlated.


Below are minimal examples illustrating:

1. **Option A**: Two separate Up-Down operators  
2. **Option B**: A single combined Up-Down operator  
3. **An AdaptiveVarianceMultivariateNormal operator**  

---

### Option A: Separate Up-Down operators

```xml
<!-- One Up-Down operator for f0 and the tree -->
<operator id="gompertzUpDown_f0" spec="operator.UpDownOperator"
          scaleFactor="0.75" weight="3.0">
    <up idref="f0"/>
    <down idref="tree"/>
</operator>
<!-- Another Up-Down operator for b and the tree -->
<operator id="gompertzUpDown_b" spec="operator.UpDownOperator"
          scaleFactor="0.75" weight="3.0">
    <up idref="b"/>
    <down idref="tree"/>
</operator>
```

### Option B: Single Up-Down operator
```xml
<operator id="gompertzUpDown_f0_b" spec="operator.UpDownOperator"
          scaleFactor="0.75" weight="3.0">
    <up idref="f0"/>
    <up idref="b"/>
    <down idref="tree"/>
</operator>

```
### AVMN operator for correlated proposals
```xml
<operator id="AVMNOperator" spec="kernel.AdaptableVarianceMultivariateNormalOperator" beta="0.05" burnin="400" initial="800" weight="2">
  <transformations id="f0Transform" spec="operator.kernel.Transform$LogitTransform">
    <f idref="f0"/>
  </transformations>
  <transformations id="bTransform" spec="operator.kernel.Transform$LogTransform">
    <f idref="b"/>
  </transformations>
</operator>

```

### Tips and Adjustments

- **Naming**  
  - If your tree is named `psi` (instead of `tree`), change `<down idref="tree"/>` to `<down idref="psi"/>`.  
  - Rename operator IDs to something more descriptive (e.g., `"upDown_f0_tree"` or `"upDown_f0_b_tree"`).


- **Tune `weight`, `scaleFactor`, etc.**  
  - Adjust these to balance acceptance rates and sampling efficiency.  
  - For more complex datasets, you might increase `weight` or refine `beta`, `burnin`, etc. in the adaptive operator.
 
### Further Guidance

- For a short tutorial on setting up updown operators in BEAST2, see [Bactrian up down operator](https://beast2-dev.github.io/hmc/hmc/Operators/BactrianUpDown/).  
- For advanced tips (e.g., example for AVMN operator), see [AVMN operators](https://www.beast2.org/2023/01/04/beast-2.6-vs-2.7-performance-benchmarking.html).



## LPhyBEAST Installation Guide

LPhyBEAST (and its extensions) are a set of add-on packages for BEAST 2, requiring both BEAST 2 and LPhy to be at their latest versions. Follow these steps to install:

### 1. Install LPhyBEAST in BEAUti

1. Launch **BEAUti**.
2. Go to **File -> Manage Packages...** to open the Package Manager.
3. In the package list, find `lphybeast` and click **Install/Upgrade**.
4. If you need to use extended models, install `LPhyBeastExt` similarly.
5. Once the download and installation complete, the Package Manager will confirm success.
6. Restart the Package Manager or BEAUti. The package should display as **Installed**.

### 2. Install LPhy

> **Note**: The `lphybeast` package does **not** include LPhy. You need to install [LPhy Studio](https://github.com/lphy-release/LPhyStudio/releases) separately if not already installed.

- Download and install **LPhy Studio** to the default path to ensure compatibility.

## Demo: The whole process 

---

This minimal example demonstrates how to use **PopFunc** within **BEAST2**.

### 1. Example Data

For convenience, we provide a simulated dataset:

- 16 taxa
- 200 bp in sequence length

You can find the Lphy script (e.g.,`demo.lphy`) in the `PopFunc/examples/`directory. It can generate the required XML and run it through **LPhyBEAST**. In this script:

We define the **substitution model** (e.g., HKY) and **clock model** (e.g., strict clock).

We define multiple population-growth functions (exponential, logistic, gompertz, plus their “expansion” versions).

We specify **discrete model indicators** (`I`, `I_A`) for switching among models.

> **Note**: 
> - You can tailor how many models to include (e.g., exponential vs. logistic vs. gompertz, each possibly with or without expansion).
> - You can also add or refine specialized operators (e.g., up-down or AVMN) to help mixing if you detect strong parameter correlations.

### 2.Running the Analysis

If Using LPhy, from a terminal you should do:

```bash
cd $PopFunc_PATH/examples/
$BEAST_PATH/bin/lphybeast demo.lphy
```

This generates demo.xml (and other intermediate files).

Then you can run BEAST manually:

```bash
$BEAST_PATH/bin/beast demo.xml
```

- Tip: If you want to use coupled MCMC option, please do:
```bash
$BEAST_PATH/bin/lphybeast \
  --mc3 \
  --chains 4 \
  --deltaTemperature 0.15 \
  --resampleEvery 1000 \
  --target 0.234 \
  demo.lphy
```
You can choose different coupled MCMC setup if you like.

### 3.Post-Processing and Visualization

- Use [Tracer](https://github.com/beast-dev/tracer/releases).
  to open the `demo.log` file.
- Confirm that ESS (Effective Sample Size) values are sufficiently large (usually > 200).
- Confirm the model indicators (`I`, `I_A`) are mixing (i.e., not stuck at one value).

### 4. Model Posterior Support

- The `demo.log` file contains columns for `I` and `I_A`.
- By examining their frequencies (or using an R/Python script), you can estimate the posterior probability of each growth model combination.

### 5. Demographic Reconstruction
Each MCMC sample indicates:
- Which model was chosen (via `I`, `I_A`). 
- The associated parameters (e.g., `r`, `b`, `N_A`, etc.).

You can reconstruct N(t) on a time grid for each sample, then compute the mean, median, and 95% HPD intervals across all samples (i.e., model-averaged). 

For the code referenced in Sections 4 and 5, please visit [this GitHub repository](https://github.com/yxu927/PopFunc-paper/tree/main/real/L86/scripts). The two scripts(`BMA.R` and `demographic.py`) are two examples provided in the `scripts` folder.




