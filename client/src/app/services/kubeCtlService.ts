import axios from "axios";

class KubeCtlService {
  async execute(command: string) {
    return (await axios.get("/api/core/kubectl")).data
  }
}

export default new KubeCtlService();