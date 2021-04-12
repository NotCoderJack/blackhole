import React, { useCallback, useState, useRef } from "react";
import { Card, CardActions, CardContent, InputBase, Typography } from "@material-ui/core"
import { makeStyles } from "@material-ui/core/styles"
import { IService } from "./types"
import terminalService from "./services/TerminalService"

interface TerminalProps {
  /** height */
  rows?: number;
  /** label */
  label?: string;
  /** callback when press enter */
  onEnter?: () => void;
  /** 
   * input process callback
   * @param {String} input 
   * @return processed output
   */
  service?: IService
}

const KEYBOARD_ENTER_KEY = "Enter";

const useStyles = makeStyles({
  root: {
    backgroundColor: '#333',
    width: '100%',
    minHeight: 90,
    maxHeight: 120,
    overflowY: 'scroll',
    borderRadius: 0,
    padding: 4
  },
  content: {
    color: '#fff',
    padding: 0
  },
  actions: {
    padding: 0,
    color: '#fff'
  },
  std: {
    whiteSpace: "pre-line"
  },
  input: {
    color: '#fff'
  }
});

/**
 * Terminal for interact with server by REST
 * @param {String} label
 * @returns JSX.Element
 */
export default function Terminal({
  label = "bash",
  rows = 6,
  service
}: TerminalProps) {
  const classes = useStyles();

  // input and outputs manage need abstract operations
  const [command, setCommand] = useState("");
  const [formatStd, setFormatStd] = useState("");
  const inputRef = useRef<HTMLInputElement>(null)

  const handleKeyDown = useCallback((e: React.KeyboardEvent<HTMLInputElement>) => {
    if (e.key !== KEYBOARD_ENTER_KEY) return;
    console.log('down');
    terminalService.service(command, service as IService, () => {
      setFormatStd(formatStd + command + "\n")
    })
  }, [command, formatStd])

  const handleChange = useCallback((e: React.ChangeEvent<HTMLInputElement>) => {
    setCommand(e.target.value)
  }, [])

  const handleClickCard = useCallback(() => {
    console.log(inputRef.current)
    inputRef.current?.focus();
  }, [inputRef])

  return (
    <Card className={classes.root} onClick={handleClickCard}>
      <CardContent className={classes.content}>
        <Typography variant="body2" component="p" className={classes.std}>
          {formatStd}
        </Typography>
      </CardContent>
      <CardActions className={classes.actions}>
        <Typography color="primary" component="span">$</Typography>
        <InputBase 
          inputRef={inputRef} 
          fullWidth 
          className={classes.input} 
          spellCheck={false}
          value={command}
          onChange={handleChange}
          onKeyDown={handleKeyDown}
        />
      </CardActions>
    </Card>
  )
}