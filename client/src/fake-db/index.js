import MockInterceptor from "./interceptor";

import "./db/invoice";
import "./db/calendarEvents";
import "./db/users";
import "./db/inbox";
import "./db/chat";
import "./db/todos";
import "./db/list";
import "./db/scrumBoard";
import "./db/notification";
import "./db/ecommerce";
import "./db/cluster";

MockInterceptor.onAny().passThrough();
