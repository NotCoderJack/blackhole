import layout1Settings from "./Layout1/Layout1Settings";
import { themeColors } from "./MatxTheme/themeColors";
import { createMuiTheme } from "@material-ui/core/styles";
import { forEach, merge } from "lodash";
import themeOptions from "./MatxTheme/themeOptions";

function createMatxThemes() {
  let themes = {} as { [key: string]: any };

  forEach(themeColors, (value, key: string) => {
    // @ts-ignore
    themes[key] = createMuiTheme(merge({}, themeOptions, value));
  });
  return themes;
}
const themes = createMatxThemes();

export const LayoutSettings = {
  activeLayout: "layout1", // layout1, layout2
  activeTheme: "purple1", // View all valid theme colors inside MatxTheme/themeColors.js
  perfectScrollbar: true,

  themes: themes,
  layout1Settings, // open Layout1/Layout1Settings.js

  secondarySidebar: {
    show: false,
    open: true,
    theme: "white" // View all valid theme colors inside MatxTheme/themeColors.js
  },
  // Footer options
  footer: {
    show: false,
    fixed: false,
    theme: "white" // View all valid theme colors inside MatxTheme/themeColors.js
  }
};
