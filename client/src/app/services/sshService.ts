import axios from "axios";

class SSHService {
  async execute(host: string, command: string) {
    return (await axios.get(`/api/core/ssh/${host}`)).data
  }
}

export default new SSHService();