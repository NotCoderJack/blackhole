import axios from "axios";
import sshService from "./sshService";
class NodeService {
  private readonly sshService
  constructor() {
    this.sshService = sshService
  }

  getNodeList = async () => {
    return (await axios.get("/api/core/nodes")).data
  }
  getNodeByHost = async (host: string) => {
    return (await axios.get(`/api/core/nodes/${host}`)).data
  }
  createNode = async (data: any) => {
    return (await axios.put("/api/core/nodes/create", data)).data
  }
  executeSSH = async (host: string, input: string) => {
    return sshService.execute(host, input)
  }
}

export default new NodeService()