## JS编码规范
<p align="left"><a href="https://github.com/feross/standard" target="_blank"><img width="200"src="https://cdn.rawgit.com/feross/standard/master/badge.svg"></a></p>

### 1. 文件注释

文件注释用来说明当前文件所实现内容，主要包括说明部分和作者部分。

- 说明部分：描述文件实现的功能、模块或接口等，如有需要增加使用说明或者范例。
- 作者部分：记录文件的开发者名称和联系方式，用以后续的维护或者交流沟通等。
- 路径部分：记录当前文件的路径，便于理解该文件所在的上下文结构。

注释范例如下图所示：

```javascript
/**
 * ------------------------------------------
 * 收货单列表数据服务文件
 * @author Alex(wyx@cn.tradeshift.com)
 * @author Kimi(kgj@cn.tradeshift.com)
 * @path src/apps/proformaManagement/service
 * ------------------------------------------
 */
 ```

### 2. 接口注释

接口注释用来描述当前定义的接口的功能及输入输出数据，主要包括说明部分、输入部分、输出部分。
- 说明部分：说明当前接口的功能，如有需要可增加使用说明或者范例。
- 输入部分：说明输入数据的类型及含义。
- 输出部分：说明输出数据的类型及含义。
注释范例如下图所示：

```javascript
/**
 * 获取单条收货单 Properties 中具体值
 * @param  {Number} [index] 数组索引
 * @param  {String} [key] 需要获取的键
 * @return {String} 需要获取的具体值
 */
_proto.getProperty = function (obj, key) {
  const properties = obj.Properties
  for (let v of properties) {
    if (key === v.scheme) return v.value
  }
}
```

### 3. 缩进

使用 2 个空格做为一个缩进层级，或者2字符的tab缩进

### 4. 命名

- 常量、枚举的属性使用全部字母大写，单词间下划线分隔的命名方式

```javascript
const DEFAULT_PAGE_SIZE = 20
```

- 一般变量，一般函数名使用（小）驼峰命名法

```javascript
let selectedCount = 0
```

- 构造函数名使用（大）驼峰/Pascal命名法

```javascript
const GrFilterModel = function (tenantId) {
  this.reset()
  this.getFilter()        
}
```

### 5. 空格
- 二元运算符两侧必须有一个空格，一元运算符与操作对象之间不允许有空格。
```javascript
let a = !arr.length
a++
a = b + c
```

- 用作代码块起始的左花括号`{`前必须有一个空格。
```javascript
// ✓ ok 
if (condition) {
}

while (condition) {
}

function funcName() {
}

// ✗ avoid 
if (condition){
}

while (condition){
}

function funcName(){
}
```

- if / else / for / while / function / switch / do / try / catch / finally 关键字后，必须有一个空格
```javascript
// ✓ ok 
if (condition) {
}

while (condition) {
}

const a = function () {
}

// ✗ avoid 
if(condition) {
}

while(condition) {
}

const a = function() {
}
```
- 在对象创建时，属性中的`:`之后必须有空格，`:`之前不允许有空格。
```javascript
// ✓ ok 
const obj = {
  a: 1,
  b: 2,
  c: 3
}

// ✗ avoid 
const obj = {
  a : 1,
  b:2,
  c :3
}
```

### 6. 语句
- 结尾不写分号
- 超过80字符最好换行
- 换行语句结尾带上运算符（`&&`等）或逗号，以免ASI机制出错

### 7. if 语句
- 保持`else`和`}{`在一行
```javascript
// ✓ ok 
if (condition) {
  // ... 
} else {
  // ... 
}

// ✗ avoid 
if (condition) {
  // ... 
}
else {
  // ...
}

// ✗ avoid
if (condition)
  console.log('a')
else
  console.log('b')
```

- if 语句块中只有一行语句时候可以不写`{}`：
```javascript
// ✓ ok 
if (options.quiet !== true) {
  console.log('done')
}

// ✓ ok 
if (options.quiet !== true) console.log('done')

// ✓ ok 
options.quiet !== true && console.log('done')

// ✓ ok 注意运算符的优先级, return 语句不要这样写
options.quiet !== true && (a = 'done')

// ✗ avoid 
if (options.quiet !== true)
  console.log('done')
```

### 8. 三目运算符

换行时候`?`和`:`运算符放在句首
```javascript
// ✓ ok 
let location = env.development ? 'localhost' : 'www.api.com'
 
// ✓ ok 
let location = env.development
  ? 'localhost'
  : 'www.api.com'

// ✓ ok 
env.development
  ? 'localhost'
  : 'www.api.com'
    
// ✓ ok 
return env.development
  ? 'localhost'
  : 'www.api.com'

// ✗ avoid 
let location = env.development ?
  'localhost' :
  'www.api.com'
```

### 9. 变量声明
每个变量都要用独立的语句声明
```javascript
// ✓ ok 
let silent = true
let verbose = true
 
// ✗ avoid 
let silent = true, verbose = true
 
// ✗ avoid 
let silent = true,
  verbose = true
```
### 10. 立即执行函数IIEF
必须以`;`开头
```javascript
// ✓ ok 
;(function () {
  window.alert('ok')
}())

// ✗ avoid 
(function () {
  window.alert('ok')
}())
```

### 11. 回调函数callback
回调函数优先使用箭头函数，除非需要绑定`this`的特殊情况
```javascript
// ✓ ok 
grList.forEach(v => {
  v.hidden = false
  v.new = 'xx'
})
```

### 12. 字符串
一律使用`''`单引号
```javascript
// ✓ ok 
console.log('hello there')

// 除非遇到特殊情况 ✓ ok 
$("<div class='box'>")
```

### IDEA ESLint配置
![pasted graphic](https://cloud.githubusercontent.com/assets/12554487/25646755/fcd1c2ea-2fed-11e7-893e-eb4202e5e15d.jpg)

**参考资料**
- [JavaScript Standard Style](http://standardjs.com/rules.html)
- [Airbnb JavaScript Style Guide](https://github.com/sivan/javascript-style-guide)
- [百度EFEJavaScript编码规范](https://github.com/ecomfe/spec/blob/master/javascript-style-guide.md)
- [网易NEJ编码规范](http://nej.netease.com/course/standard/index)