import MockInterceptor from "../interceptor";
import Mock from "mockjs";
const Random = Mock.Random;
const ListDB = Mock.mock({
  'list|1-10': [{
    'id|+1': 1,
    "name": "@ctitle(10)",
    "desc": "@cword('我是集群我是集群我是集群我是集群我是集群', 4)",
    "createTime": "@date",
  }]
});
const ItemById = Mock.mock({
  'id': Random.id(),
  "name": Random.cname(),
  "createTime": Random.date(),
});

MockInterceptor.onGet("/api/cluster/list").reply(config => {
  const response = ListDB.list;
  console.log(response)
  return [200, response];
});

MockInterceptor.onGet(/api\/cluster\/list\/\d+/).reply(config => {
  console.log(config)
  return [200, ItemById]
});

MockInterceptor.onPut("/api/cluster/create").reply(config => {
  return [200, {message: 'success'}]
});