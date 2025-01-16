# BEAST PopFunc

**BEAST PopFunc** is a package for the **BEAST2** that enables Bayesian inference of parametric population-growth models under a flexible model-averaging framework. By using discrete indicator variables, PopFunc allows users to explore multiple candidate demographic models in a single run without requiring prior commitment to one specific growth function.

## Citation

If you use **PopFunc** in your research, please cite:




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




