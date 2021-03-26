import axios from "axios";
class ClusterService {
  getClusterList = async () => {
    return (await axios.get("/api/cluster/list")).data
  }
  getClusterById = async (id: number) => {
    return (await axios.get(`/api/cluster/list/${id}`)).data
  }
  createCluster = async (data: any) => {
    return (await axios.put("/api/cluster/create", data)).data
  }
}

export default new ClusterService()