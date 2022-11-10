import { useQuery } from "@tanstack/react-query";
import { instanceRepository } from "../repositories/instance-repository";
export type FilterType = "all" | "approved" | "unapproved";

export const useInstancesQuery = ({filterType}: {filterType: FilterType}) => {
  return useQuery({queryKey: ['getInstances', filterType], queryFn: async () => {
      const res = await instanceRepository.getInstances()
    if (filterType === "all") {
      return res;
    } else if (filterType === "approved") {
      return res.filter((i) => i.publishedAt != null)
    } else if (filterType === "unapproved") {
      return res.filter((i) => i.publishedAt == null)
    }
  }});
}

export const useInstanceDetailQuery = ({instanceId}: {instanceId: string}) => {
  return useQuery({queryKey: ['getInstanceDetail', instanceId], queryFn: () => {
    return instanceRepository.get(instanceId);
  }});
}

