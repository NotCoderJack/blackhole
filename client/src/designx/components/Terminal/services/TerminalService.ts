import { IService } from "../types"
class TerminalService {
  service(input: string, s: IService, callback?: (data?: any) => void) {
    s(input).then(res => {
      callback && callback(res)
    });
  }
}
export default new TerminalService()