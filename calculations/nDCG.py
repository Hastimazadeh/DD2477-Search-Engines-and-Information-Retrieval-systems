# for section 3.2

import math

def calculate_dcg(relevance_scores):
    n = len(relevance_scores)
    dcg = relevance_scores[0]
    for i in range(1, n):
        dcg += relevance_scores[i] / math.log2(i + 2)
    return dcg

def calculate_idcg(relevance_scores):
    relevance_scores_sorted = sorted(relevance_scores, reverse=True)
    idcg = relevance_scores_sorted[0]
    for i in range(1, len(relevance_scores_sorted)):
        idcg += relevance_scores_sorted[i] / math.log2(i + 2)
    return idcg

def calculate_ndcg(relevance_scores):
    dcg = calculate_dcg(relevance_scores)
    idcg = calculate_idcg(relevance_scores)
    ndcg = dcg / idcg if idcg > 0 else 0
    return ndcg

relevance_scores_first = [1, 0, 0, 1, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 1, 0, 0, 1, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 1, 0, 0, 0]
relevance_scores_third = [1, 1, 0, 0, 0, 0, 1, 1, 0, 1, 1, 2, 0, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1,1, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 1, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0]


dcg = calculate_dcg(relevance_scores_first)
print("DCG for first part: ", dcg)

idcg = calculate_idcg(relevance_scores_first)
print("IDCG for first part: ", idcg)

ndcg = calculate_ndcg(relevance_scores_first)
print("nDCG for first part: ", ndcg)

dcg = calculate_dcg(relevance_scores_third)
print("DCG for third part: ", dcg)

idcg = calculate_idcg(relevance_scores_third)
print("IDCG for third part: ", idcg)

ndcg = calculate_ndcg(relevance_scores_third)
print("nDCG for third part: ", ndcg)