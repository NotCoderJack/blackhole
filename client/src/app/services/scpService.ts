import axios from "axios";

class SSHService {
  async execute(command: string) {
    return (await axios.get("/api/core/scp")).data
  }
}

export default new SSHService();