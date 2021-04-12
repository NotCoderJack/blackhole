import MockInterceptor from "../interceptor";
import Mock from "mockjs";
const Random = Mock.Random;
const ListDB = Mock.mock({
  'list|1-10': [{
    'id|+1': 1,
    "name": "@ctitle(10)",
    "desc": "@cword('节点1节点2节点3节点4节点5节点6节点7节点8节点9', 3)",
    "createTime": "@date",
    "host": "@ip"
  }]
});
const ItemById = Mock.mock({
  'id': Random.id(),
  "name": Random.title(),
  "createTime": Random.date(),
  "host": Random.ip()
});

MockInterceptor.onGet("/api/core/nodes").reply(config => {
  const response = ListDB.list;
  console.log(response)
  return [200, response];
});

MockInterceptor.onGet(/api\/core\/nodes\/\d+/).reply(config => {
  console.log(config)
  return [200, ItemById]
});

MockInterceptor.onPut("/api/core/nodes").reply(config => {
  return [200, {message: 'success'}]
});