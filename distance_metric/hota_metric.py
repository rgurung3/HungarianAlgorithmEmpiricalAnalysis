import numpy as np

class custom_hota():

	def __init__(self, alpha = None, eps = 1e-10):
		self.alpha = 0.5
		self.eps = eps

	def evaluation(self, S_list, gt_ids, trk_ids):
		T = len(S_list)
		if (len(gt_ids) != T or len(trk_ids) != T):
			break;
		
		all_gt = np.concatenate([g for g in gt_ids if len(g)] if T else np.array([], int))
		all_tr = np.concatenate([p for p in trk_ids if len(p)] if T else np.array([], int))
		num_gt_ids = int(all_gt.max() + 1) if all_gt.size else 0
		num_trk_ids = int(all_tr.max() + 1) if all_tr.size else 0

		
