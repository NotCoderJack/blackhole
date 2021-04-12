import React, { useState, useRef, useEffect, useMemo, useCallback } from "react"
import { Box, Typography, Button, ListItem,  LinearProgress} from '@material-ui/core';
import uploadFileService from "app/services/uploadFileService"

interface UploadProps {
  children: React.ReactElement,
  showProgress?: boolean,
  /** post action url */
  action?: string
  /** custom upload */
  uploader?: () => {}
  multiple?: boolean
  fileList?: UploadFile[],
  /** show upload btn if user control when to upload */
  showUploadBtn?: boolean,
}

const enum UploadStatus {
  'uploading' = 'uploading',
  'done' = 'done',
  'error' = 'error',
  'removed' = 'removed'
}
interface UploadFile extends File {
  url: string;
  status: UploadStatus;
}

/**
 * File upload 
 * @returns 
 */
export default function Upload({
  children,
  showProgress = false,
  multiple = false,
  fileList = []
}: UploadProps) {
  const fileInputRef = useRef<HTMLInputElement>(null)
  const [selectedFiles, setSelectedFiles] = useState<UploadFile[]>([]);
  const [progress, setProgress] = useState<number>(0);

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const files = e.target.files;
    setSelectedFiles(null === files ? []: processOriginFiles(e.target.files!))
    console.log(e.target.files)
  }
  const processOriginFiles = (files: FileList) => {

    return Array.from(files).map(item => {
      return {
        ...item,
        url: '',
        status: UploadStatus.uploading
      } as UploadFile
    })
  }
  const openFileDialog = useCallback((e: React.MouseEvent<HTMLElement> | React.KeyboardEvent<HTMLElement>) => {
    if (!fileInputRef.current) {
      return;
    }
    if (children && children.type === 'button') {
      const parent = fileInputRef.current.parentNode as HTMLInputElement;
      parent.focus();
      parent.querySelector('button')?.blur();
    }
    fileInputRef.current.click();
  }, [fileInputRef.current, children]);

  useEffect(() => {
    setSelectedFiles(fileList)
  }, [fileList])

  const events = useMemo(() => {
    return {
      onClick: openFileDialog
    }
  }, [openFileDialog])

  const handleUpload = () => {
    // do upload things
  }

  return (
    <div className="mg20">
      {showProgress && (
        <Box className="mb25" display="flex" alignItems="center">
          <Box width="100%" mr={1}>
            <LinearProgress variant="determinate" value={progress} />
          </Box>
          <Box minWidth={35}>
            <Typography variant="body2" color="textSecondary">{`${progress}%`}</Typography>
          </Box>
        </Box>)
      }

      <Typography {...events} component="span" role="button">
        <input
          id="btn-upload"
          name="btn-upload"
          style={{ display: 'none' }}
          type="file"
          ref={fileInputRef}
          multiple={multiple}
          onClick={e => e.stopPropagation()}
          onChange={handleFileChange}
        />
        {children}
      </Typography>
      <ul className="list-group">
        {fileList?.map((file, index) => (
            <ListItem
              divider
              key={index}>
              <a href={file.url}>{file.name}</a>
            </ListItem>
          ))}
      </ul>
      <Button color="primary" variant="contained" onClick={handleUpload}>Upload</Button>
    </div >
  );
}