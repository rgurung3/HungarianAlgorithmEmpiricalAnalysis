#!/usr/bin/env python

import heapq
import numpy
from numpy import random
from scipy.optimize import linear_sum_assignment
from timer import Timer
import math

cache_hits,cache_misses = 0,0

def enumerator(cost_matrix,k):
    global cache_hits, cache_misses

    atimer = Timer(verbose=False)
    n = len(cost_matrix)
    all_rows = list(range(n)) # list of all row indices
    all_cols = set(range(n))  # set of all column indices

    # initial node
    depth = 0
    path = []
    with atimer:
        rows,cols = linear_sum_assignment(cost_matrix)
    cost = cost_matrix[rows,cols].sum()
    node = (depth,cost,path)

    # initialize data structures
    top_k = []
    pq = []  # priority queue
    heapq.heappush(pq,node)
    cache = { (): 0 }

    while pq and len(top_k) < k:
        current_depth,current_cost,current_path = heapq.heappop(pq)
        next_row = len(current_path)
        if next_row == n:
            top_k.append((current_cost,current_path))
            continue
        remaining_cols = all_cols.difference(current_path)
        new_path_rows = all_rows[:next_row+1]
        sub_rows = all_rows[next_row+1:] # for sub-matrix indices
        for col in remaining_cols:
            new_path = current_path + [col]
            sub_cols = tuple(sorted(remaining_cols.difference([col])))
            if sub_cols in cache:
                cache_hits += 1
                sub_cost = cache[sub_cols]
            else:
                cache_misses += 1
                sub_indices = numpy.ix_(sub_rows,sub_cols)
                sub_cost_matrix = cost_matrix[sub_indices]
                with atimer:
                    rows,cols = linear_sum_assignment(sub_cost_matrix)
                sub_cost = sub_cost_matrix[rows,cols].sum()
                cache[sub_cols] = sub_cost
            prefix_cost = cost_matrix[new_path_rows,new_path].sum()
            new_cost = prefix_cost + sub_cost
            depth = -len(new_path)
            node = (depth,new_cost,new_path)
            heapq.heappush(pq,node)

    print("timer: %.4f (%d calls)" % (atimer.total_time,atimer.total_calls))
    return top_k, atimer

def brute_force_enumerator(cost_matrix):
    from itertools import permutations

    n = len(cost_matrix)
    all_rows = list(range(n))

    all_assignments = []
    for pi in permutations(all_rows):
        cost = cost_matrix[all_rows,pi].sum()
        all_assignments.append((cost,list(pi)))

    all_assignments.sort()
    return all_assignments

if __name__ == '__main__':
    seed=0
    n=10
    k=math.factorial(n)
    #k=10

    # random matrix
    random.seed(seed)
    cost_matrix = random.randint(10,size=(n,n))
    print("---COST_MATRIX:")
    print(cost_matrix)

    with Timer("enumerator"):
        top_k = enumerator(cost_matrix,k)
        #top_k.sort() # sort assignments by lexicographic order

    print("cache hits: %d" % cache_hits)
    print("cache miss: %d" % cache_misses)

    with Timer("brute force"):
        all_k = brute_force_enumerator(cost_matrix)

    top_k.sort()
    if top_k == all_k[:k]:
        print("check ok")
    else:
        print("NOT OK")
        import pdb; pdb.set_trace()
        pass

