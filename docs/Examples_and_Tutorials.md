# Examples and Tutorials

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




