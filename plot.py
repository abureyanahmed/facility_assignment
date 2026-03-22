import matplotlib.pyplot as plt
import numpy as np

# -----------------------------
# Plot Function
# -----------------------------
def plot_capacity(capacities, greedy, optfill, title):
    plt.figure(figsize=(6, 4))

    plt.plot(capacities, greedy, marker='o', linewidth=2, label='Greedy')
    plt.plot(capacities, optfill, marker='s', linewidth=2, label='Optimal-Fill')

    plt.xlabel("Capacity (u)")
    plt.ylabel("Max Ratio")
    plt.title(title)

    plt.legend()
    plt.grid(True, linestyle='--', alpha=0.6)

    plt.tight_layout()
    plt.show()

