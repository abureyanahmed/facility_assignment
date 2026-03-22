import random
import numpy as np
import networkx as nx

# -----------------------------
# Core Dataset Generators
# -----------------------------

def generate_line_instance(F, u, seed=None):
    if seed is not None:
        random.seed(seed)
        np.random.seed(seed)

    n_customers = F * u
    L = 10 * F

    facilities = np.random.uniform(0, L, F)
    customers = np.random.uniform(0, L, n_customers)

    return facilities, customers


def generate_plane_instance(F, u, seed=None):
    if seed is not None:
        random.seed(seed)
        np.random.seed(seed)

    n_customers = F * u
    L = 10 * F

    facilities = np.random.uniform(0, L, (F, 2))
    customers = np.random.uniform(0, L, (n_customers, 2))

    return facilities, customers


def generate_graph_instance(F, u, seed=None):
    if seed is not None:
        random.seed(seed)
        np.random.seed(seed)

    n_vertices = 4 * F * u
    n_customers = F * u

    # Generate connected Erdős–Rényi graph
    while True:
        p = min(1.0, (np.log(n_vertices) / n_vertices) * 2)
        G = nx.erdos_renyi_graph(n_vertices, p, seed=seed)

        if nx.is_connected(G):
            break

    # Assign random weights
    for (u_, v_) in G.edges():
        G[u_][v_]['weight'] = random.uniform(1, 10)

    vertices = list(G.nodes())
    facilities = random.sample(vertices, F)
    customers = random.choices(vertices, k=n_customers)

    return G, facilities, customers


# -----------------------------
# Distance Functions
# -----------------------------

def euclidean_1d(a, b):
    return abs(a - b)


def euclidean_2d(a, b):
    return np.linalg.norm(a - b)


def graph_distance(G, source, target):
    return nx.shortest_path_length(G, source, target, weight='weight')


# -----------------------------
# Placeholder Assignment Logic
# -----------------------------
# Replace these with your actual algorithms

def greedy_assignment(facilities, customers, capacity, dist_fn):
    remaining = [capacity] * len(facilities)
    assignments = []

    for c in customers:
        best_f = None
        best_dist = float('inf')

        for i, f in enumerate(facilities):
            if remaining[i] > 0:
                d = dist_fn(c, f)
                if d < best_dist:
                    best_dist = d
                    best_f = i

        if best_f is not None:
            remaining[best_f] -= 1
            assignments.append((c, best_f, best_dist))
        else:
            assignments.append((c, None, None))

    return assignments


def count_non_trivial(assignments):
    # "Non-trivial" = distance > 0 (you can refine this definition)
    return sum(1 for (_, _, d) in assignments if d is not None and d > 0)


# -----------------------------
# Filtering Wrapper
# -----------------------------

def generate_filtered_instance(setting, F, u, min_non_trivial=5, seed=None):
    while True:
        if setting == "line":
            facilities, customers = generate_line_instance(F, u, seed)
            dist_fn = euclidean_1d

        elif setting == "plane":
            facilities, customers = generate_plane_instance(F, u, seed)
            dist_fn = euclidean_2d

        elif setting == "graph":
            G, facilities, customers = generate_graph_instance(F, u, seed)
            dist_fn = lambda a, b: graph_distance(G, a, b)
        else:
            raise ValueError("Unknown setting")

        # Run Greedy
        greedy_assign = greedy_assignment(facilities, customers, u, dist_fn)
        greedy_non_trivial = count_non_trivial(greedy_assign)

        # Placeholder for Optimal-Fill (reuse greedy for now)
        opt_assign = greedy_assignment(facilities, customers, u, dist_fn)
        opt_non_trivial = count_non_trivial(opt_assign)

        if greedy_non_trivial >= min_non_trivial and opt_non_trivial >= min_non_trivial:
            if setting == "graph":
                return G, facilities, customers
            else:
                return facilities, customers
