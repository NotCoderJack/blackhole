import axios from "axios";
import kubectlService from "./kubeCtlService"
class ClusterService {
  getClusterList = async () => {
    return (await axios.get("/api/clusters")).data
  }
  getClusterById = async (id: number) => {
    return (await axios.get(`/api/clusters/${id}`)).data
  }
  createCluster = async (data: any) => {
    return (await axios.put("/api/clusters/create", data)).data
  }
  executeKubectl = async (command: string) => {
    return (await axios.get("/api/core/kubectl")).data
  }
}

export default new ClusterService()