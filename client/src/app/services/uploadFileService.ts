import axios from "axios";
class UploadFileService {
  private __files: File[] = [];
  private __uploading = [];

  addFile(file: File) {
    this.__files.push(file)
  }
  upload(file: File, onUploadProgress: () => void) {

  }
}

export default new UploadFileService();