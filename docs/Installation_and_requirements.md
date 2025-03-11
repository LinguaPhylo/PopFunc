# Installation & Requirements

## 1. System Prerequisites

- **Java 17** or higher.
- **BEAST 2.7**.  
  PopFunc is developed and tested with BEAST v2.7.x.  
  For official releases, see [BEAST2’s GitHub](https://github.com/CompEvol/beast2).

We also recommend installing:
- The **CoupledMCMC** package (if you plan to use Metropolis-coupled MCMC).
- **LinguaPhylo** and **LPhyBEAST** (if you want to integrate PopFunc with LPhy scripts).

## 2. Installing PopFunc

### Option A: Using BEAST2 Package Manager (Recommended)

1. Launch **BEAUti** (part of BEAST2).
2. Click **File -> Manage Packages...** to open the Package Manager.
3. Locate **PopFunc** in the list and click **Install/Upgrade**.
4. Once installation finishes, restart BEAUti. **PopFunc** should appear as installed.

### Option B: Command-Line Installation

In your BEAST2 installation directory (e.g., `~/beast/bin` on Linux/Mac), run:
```bash
./packagemanager -add PopFunc
```
Afterwards, confirm PopFunc is listed by running:

```bash
./packagemanager -list
```