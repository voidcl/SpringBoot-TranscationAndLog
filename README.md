
##**Spring Boot 事务以及日志系统简介**

###事务（transcation）


####事务配置
- 依赖（pom.xml)
工程基于maven构建，方便依赖的管理
``` <!-- Spring Boot 启动父依赖 -->
<parent>
<groupId>org.springframework.boot</groupId>
<artifactId>spring-boot-starter-parent</artifactId>
<version>1.5.8.RELEASE</version>
</parent>
<!-- spring boot 项目启动必须引入的web依赖 -->
<dependencies>
	<dependency>
			<groupId>mysql</groupId>
			<artifactId>mysql-connector-java</artifactId>
			<scope>runtime</scope>
		</dependency>
	<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-test</artifactId>
			<scope>test</scope>
	</dependency>
	<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-jdbc</artifactId>
	</dependency>
	<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-logging</artifactId>
	</dependency>
	<dependency>
			<groupId>org.mybatis</groupId>
			<artifactId>mybatis-spring</artifactId>
			<version>1.3.1</version>
	</dependency>
	<dependency>
			<groupId>org.mybatis</groupId>
			<artifactId>mybatis</artifactId>
			<version>3.4.4</version>
	</dependency>
	<dependency>
			<groupId>tk.mybatis</groupId>
			<artifactId>mapper</artifactId>
			<version>3.4.0</version>
	</dependency>
	</dependencies> 
```
- 默认事务

#####spring默认事务的配置，就是在mybatis的基础上加上两个注解。 
	-	@EnableTransactionManagement
	-	@Transactional
#####前者需要添加到SpringBoot启动文件上，也就是拥有@SpringBootApplication注解的java文件中
	
```
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@EnableTransactionManagement
public class SimpleApplication {
	public static void main(String[] args) {
		SpringApplication.run(SimpleApplication.class, args);
	}
}
```

#####后者添加在需要的方法上，同时这也是推荐的操作
	
```
	@Transactional(value = "primaryTestManager")
    public void delayUpdate(){
    }
```
#####当然也可以添加在类上，结果是整个类都被添加了事务。

- 自定义事务

#####从上面@transcational注解的使用，就可以看出事务是可以自定义的，delayUpdate就指定了“primaryTestManager”来作为该函数的事务管理器，在数据源配置文件中添加以下代码
```	
	@Bean
    public PlatformTransactionManager primaryTestManager(@Qualifier("primaryDataSource") DataSource dataSource)
    {
        return new DataSourceTransactionManager(dataSource);
    }
```	
	
就完成了一个叫primaryTestManager的事务管理器配置，需要注意的是在配置了额外的事务管理器后，需要制定一个事务管理器，否则程序会抛出不知道选择哪一个事务管理器的异常。
另外，此功能一般配合多数据源使用，如以上代码中的@Qualifier("primaryDataSource")就是一个已经配置好的叫做primaryDataSource的数据源。
####事务使用
- @transcational配置说明

@transcational的属性
|属性|类型|描述|
|---|----|---|
|value|String|可选的限定描述符，指定使用的事务管理器|
|propagation|enum: Propagation|可选的事务传播行为设置|
|isolation|enum: Isolation|可选的事务隔离级别设置|
|readOnly|boolean|读写或只读事务，默认读写|
|timeout|int (in seconds granularity)|事务超时时间设置|
|rollbackFor|Class对象数组必须继承自Throwable|导致事务回滚的异常类数组|
|rollbackForClassName|类名数组必须继承自Throwable|导致事务回滚的异常类名字数组|
|noRollbackFor|Class对象数组必须继承自Throwable|不会导致事务回滚的异常类数组|
|noRollbackForClassName|类名数组必须继承自Throwable|不会导致事务回滚的异常类名字数组|
||||


1. 事务传播行为（propagarion）
|         |            | |
| -----------------|-------------|
| Propagation.REQUIRED|如果有事务， 那么加入事务， 没有的话新建一个(默认情况下) |
|Propagation.NOT_SUPPORTED|容器不为这个方法开启事务|
|Propagation.REQUIRES_NEW|不管是否存在事务，都创建一个新的事务，原来的挂起，新的执行完毕，继续执行老的事务|
|Propagation.MANDATORY|必须在一个已有的事务中执行，否则抛出异常|
|Propagation.NEVER|必须在一个没有的事务中执行，否则抛出异常(与Propagation.MANDATORY相反)|
|Propagation.SUPPORTS|如果其他bean调用这个方法，在其他bean中声明事务，那就用事务。如果其他bean没有声明事务，那就不用事务|
|||
2.  事务隔离级别（isolation）

||||
|-|-|
|Isolation.READ_UNCOMMITTED|读取未提交数据|
|Isolation.READ_COMMITTED|读取已提交数据|
|Isolation.REPEATABLE_READ|可重复读|
|Isolation.SERIALIZABLE|串行化|

以下是一些例子
```
@Transactional
            (value = "primaryTestManager",
            isolation = Isolation.REPEATABLE_READ,
            propagation = Propagation.NESTED)
    public void printInfo(){
    }
```

```
@Transactional(rollbackFor=Exception.class) 
public void methodName() {
}
```

```
@Transactional(noRollbackFor=Exception.class)
public ItimDaoImpl getItemDaoImpl() {
}
```


- 注意事项

	- spring AOP在处理事务上的机制
	
	spring无法拦截到内部嵌套调用的方法，如
	```
	public void run(){
	public void doSomething1() {  
        System.out.println("doSomething1“);  
        doSomething2();  
    }  //此处dosomething2方法无法被拦截

    public void doSomething2() {  
        System.out.println("doSomething2");      
	}
	```
	所以不要出现“自调用”的情况，这是Spring文档中推荐的“最佳”方案。
	或者spring提供了一个增强AOP的选项
	`<aop:aspectj-autoproxy expose-proxy="true" />`
	相应的代码也要做修改
	```
	((CustomerService)AopContext.currentProxy()).doSomething2();
	```
	- 数据库与框架上的一些细节
	
	首先是cache的问题，数据库提供了cache的机制来让数据查询更有效率，但是有些时候我们需要将它关闭，具体的步骤就是在数据库根目录下里找到my.ini，my.cnf（以mysql为例）的配置文件，如果不存在可以自己新建一个，往其中添加自定义配置
	```
[mysqld]
query_cache_size=0
	```
	就可以手动关闭。而mybatis同样拥有缓存机制，这就需要在mapper里的查询语句后添加一些变量
	``` 
	<select id="selectAllStu" resultMap="StuResult" flushCache="true" useCache="false">
	```
	使得查询能重新访问数据库，而不是访问第一次查询的快照。
即便如此，在RR隔离级别下，同一事务的所有一致性读只会读取第一次查询时创建的快照。
	
	其次，关于“幻读”的问题，很多时候我们认为数据库四层隔离级别是循序渐进的，这一层隔离级别所处理的问题必然是前一层所发生的，然后理所当然得认为幻读会发生在RR级别，实际上这是错误的，以下是wiki上关于幻读（phantom read）的说明
	> This can occur when range locks are not acquired on performing a SELECT ... WHERE operation. The phantom reads anomaly is a special case of Non-repeatable reads when Transaction 1 repeats a ranged SELECT ... WHERE query and, between both operations, Transaction 2 creates (i.e. INSERT) new rows (in the target table) which fulfill that WHERE clause.
	
	幻读其实是不可重复读的一种特殊的例子（a special case of Non-repeatable reads）, 所以RR级别不会出现

####事务总结
	首先SpringBoot事务是基于数据库事务之上的，只不过提供了一些可自定的选项，比如隔离级别以及传播等级，解放了程序员需要亲自前往数据库设置的工作，转而在注解或者xml文件中配置变量，让人感觉更加亲切。其次，SpringBoot事务依靠AOP来实现，这点也是特别需要注意的地方，如果在方法内调用方法，必须要开启AOP增强，其实也就是spring让程序员使用它的一个类方法来调用自己类内部的方法，本质上还是一样的。最后，在工程中使用事务是非常有必要的，能有效增加数据库的安全性，让代码逻辑更加完善。
	
###日志（logback）

	logback构建于log4j之上，logback可以说就是一个更好的log4j，所以我们采用了logback来进行日志管理。
####基本配置
#####根据官网上的说法

>1.Logback tries to find a file called logback-test.xml in the classpath.
2.If no such file is found, logback tries to find a file called logback.groovy in the classpath.
3.If no such file is found, it checks for the file logback.xml in the classpath..
4.If no such file is found, service-provider loading facility (introduced in JDK 1.6) is used to resolve the implementation of com.qos.logback.classic.spi.Configurator interface by looking up the file META-INF\services\ch.qos.logback.classic.spi.Configurator in the class path. Its contents should specify the fully qualified class name of the desired Configurator implementation.
5.If none of the above succeeds, logback configures itself automatically using the BasicConfigurator which will cause logging output to be directed to the console.

logback会自动寻找以上位置的配置文件来进行配置的加载，其实只要在resource文件夹下新建一个logback-test.xml文件就可以进行配置了。

- 基础配置
	logback跟其他日志管理一样，拥有多种日志输出级别，所以要设置一个基础级别来控制输出，其他低于这个级别的日志将不会进行输出，

	|||||||||
|-|-|-|-|-|-|
||TRACE	|DEBUG	|INFO	|WARN	|ERROR	|OFF|
|TRACE	|YES	|NO	|NO	|NO	|NO	|NO
|DEBUG	|YES	|YES	|NO	|NO	|NO	|NO
|INFO	|YES	|YES	|YES	|NO	|NO	|NO
|WARN	|YES	|YES	|YES	|YES	|NO	|NO
|ERROR	|YES	|YES	|YES	|YES	|YES	|NO

	这一功能通过filter来实现，后面会具体介绍。
	
	需要注意的是debug等级默认是不会输出的，我们调试的时候要是需要输出debug等级的日志，则需要在xml第一个标签上添加一条属性
	
		<configuration debug="true">
	
	这样就可以进行debug级别日志的输出

- sql语句，事务打印
这其实是很多使用者最需要的功能，每种不同的框架有不同的配置方法，这里列出mybatis的配置
```
<logger name="com.ibatis" level="DEBUG" />
    <logger name="com.ibatis.common.jdbc.SimpleDataSource" level="DEBUG" />
    <logger name="com.ibatis.common.jdbc.ScriptRunner" level="DEBUG" />
    <logger name="com.ibatis.sqlmap.engine.impl.SqlMapClientDelegate" level="DEBUG" />

    <logger name="java.sql.Connection" level="DEBUG" />
    <logger name="java.sql.Statement" level="DEBUG" />
    <logger name="java.sql.PreparedStatement" level="DEBUG" />
    <logger name="voidcl.simple.dao.AData.*" level="DEBUG" additivity="false"></logger>
    //这条语句是将对应的mapper操作纳入至logback体系中，这样就可以看到输出。
```
####标签说明及高级操作
- appender

	 ConsoleAppender
	把日志输出到控制台，有以下子节点：
	>`<encoder>：`对日志进行格式化。
`<target>：`字符串System.out(默认)或者System.err

	 FileAppender
	把日志添加到文件，有以下子节点：
　 
>　`<file>：`被写入的文件名，可以是相对目录，也可以是绝对目录，如果上级目录不存在会自动创建，没有默认值。
　`<append>：`如果是 true，日志被追加到文件结尾，如果是 false，清空现存文件，默认是true。
　`<encoder>：`对记录事件进行格式化。
　`<prudent>：`如果是 true，日志会被安全的写入文件，即使其他的FileAppender也在向此文件做写入操作，效率低，默认是 false。

　　　　　　
RollingFileAppender
	滚动记录文件，先将日志记录到指定文件，当符合某个条件时，将日志记录到其他文件。有以下子节点：
	>`<file>`：被写入的文件名，可以是相对目录，也可以是绝对目录，如果上级目录不存在会自动创建，没有默认值。
`<append>`：如果是 true，日志被追加到文件结尾，如果是 false，清空现存文件，默认是true。	
`<rollingPolicy>`:当发生滚动时，决定RollingFileAppender的行为，涉及文件移动和重命名。属性class定义具体的滚动策略类
　　　　　class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy"： 最常用的滚动策略，它根据时间来制定滚动策略，既负责滚动也负责出发滚动。有以下子节点：
`<fileNamePattern>`：必要节点，包含文件名及“%d”转换符，“%d”可以包含一个java.text.SimpleDateFormat指定的时间格式，如：%d{yyyy-MM}。如果直接使用 %d，默认格式是 yyyy-MM-dd。RollingFileAppender的file字节点可有可无，通过设置file，可以为活动文件和归档文件指定不同位置，当前日志总是记录到file指定的文件（活动文件），活动文件的名字不会改变；
如果没设置file，活动文件的名字会根据fileNamePattern 的值，每隔一段时间改变一次。“/”或者“\”会被当做目录分隔符。	
`<maxHistory>`:可选节点，控制保留的归档文件的最大数量，超出数量就删除旧文件。假设设置每个月滚动，且`<maxHistory>`是6，则只保存最近6个月的文件，删除之前的旧文件。注意，删除旧文件是，那些为了归档而创建的目录也会被删除。
class="ch.qos.logback.core.rolling.SizeBasedTriggeringPolicy"： 查看当前活动文件的大小，如果超过指定大小会告知RollingFileAppender 触发当前活动文件滚动。只有一个节点:
`<maxFileSize>`:这是活动文件的大小，默认值是10MB。
`<prudent>`：当为true时，不支持FixedWindowRollingPolicy。支持TimeBasedRollingPolicy，但是有两个限制，1不支持也不允许文件压缩，2不能设置file属性，必须留空。	
`<triggeringPolicy >`: 告知 RollingFileAppender 合适激活滚动。class="ch.qos.logback.core.rolling.FixedWindowRollingPolicy" 根据固定窗口算法重命名文件的滚动策略。有以下子节点：
`<minIndex>`:窗口索引最小值
`<maxIndex>`:窗口索引最大值，当用户指定的窗口过大时，会自动将窗口设置为12。
`<fileNamePattern>`:必须包含“%i”例如，假设最小值和最大值分别为1和2，命名模式为 mylog%i.log,会产生归档文件mylog1.log和mylog2.log。还可以指定文件压缩选项，例如，mylog%i.log.gz 或者 没有log%i.log.zip


	这个appender有点复杂，所以下面放上简单的样例 
```
<!-- 日志记录器，日期滚动记录 -->
<appender name="FILEINFO" class="ch.qos.logback.core.rolling.RollingFileAppender">
<!-- 正在记录的日志文件的路径及文件名 -->
<file>${LOG_PATH}/${APPDIR}/xcloud-print_log_info.log</file>
<!-- 日志记录器的滚动策略，按日期，按大小记录 -->
<rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
<!-- 归档的日志文件的路径，例如今天是2013-12-21日志，当前写的日志文件路径为file节点指定，可以将此文件与file指定文件路径设置为不同路径，从而将当前日志文件或归档日志文件置不同的目录。
            而2013-12-21的日志文件在由fileNamePattern指定。%d{yyyy-MM-dd}指定日期格式，%i指定索引 -->        <fileNamePattern>${LOG_PATH}/${APPDIR}/info/xcloud-print_log-info-%d{yyyy-MM-dd}.%i.log</fileNamePattern>
<!-- 除按日志记录之外，还配置了日志文件不能超过2M，若超过2M，日志文件会以索引0开始，
            命名日志文件，例如log-error-2013-12-21.0.log -->
<timeBasedFileNamingAndTriggeringPolicy class="ch.qos.logback.core.rolling.SizeAndTimeBasedFNATP">
<maxFileSize>12MB</maxFileSize>
</timeBasedFileNamingAndTriggeringPolicy>
</rollingPolicy>
<!-- 追加方式记录日志 -->
<append>true</append>
<!-- 日志文件的格式 -->
<encoder class="ch.qos.logback.classic.encoder.PatternLayoutEncoder">
<pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} %-5level --- [%thread] %logger Line:%-3L - %msg%n</pattern>
<charset>utf-8</charset>
</encoder>
<!-- 此日志文件只记录info级别的 -->
<filter class="ch.qos.logback.classic.filter.LevelFilter">
<level>info</level>
<onMatch>ACCEPT</onMatch>
<onMismatch>DENY</onMismatch>
</filter>
</appender>
```

其实appender还同样支持自定义，只要implements  **ch.qos.logback.core.Appender **接口就能写出自己的appender

- encoder
	对记录事件进行格式化。负责两件事，一是把日志信息转换成字节数组，二是把字节数组写入到输出流。
PatternLayoutEncoder 是唯一有用的且默认的encoder ，有一个`<pattern>`节点，用来设置日志的输入格式。使用“%”加“转换符”方式，如果要输出“%”，则必须用“\”对“\%”进行转义。

- filter
这是一个拦截器，可以控制各种log输出，比如限制等级，或者跨级输出，具体看以下例子

```
<filter class="ch.qos.logback.classic.filter.LevelFilter">
<level>info</level>
<onMatch>ACCEPT</onMatch>
<onMismatch>DENY</onMismatch>
</filter>
</appender>
```

写法比较简单，功能就是将对应的appender进行判断输出，如果等级满足输出等级则输出


```
<configuration>

  <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
    <filter class="ch.qos.logback.core.filter.EvaluatorFilter">      
      <evaluator> <!-- defaults to type ch.qos.logback.classic.boolex.JaninoEventEvaluator -->
        <expression>return message.contains("billing");</expression>
      </evaluator>
      <OnMismatch>NEUTRAL</OnMismatch>
      <OnMatch>DENY</OnMatch>
    </filter>
    <encoder>
      <pattern>
        %-4relative [%thread] %-5level %logger - %msg%n
      </pattern>
    </encoder>
  </appender>

  <root level="INFO">
    <appender-ref ref="STDOUT" />
  </root>
</configuration>
```
此处filter完成的是将用户“billing”输出的信息等级人为提高，所以实际环境中使用logback能将某个用户的日志等级单独隔离出来，容易查错。而且filter同样支持自定义化，可以看下面的链接
[更多的filter样例与使用](https://logback.qos.ch/manual/filters.html)

- 参数化
logback支持类似于占位符的变量替换功能，即如果输出的msg里面带有{}符号且括号中间不带其他字符，那么logback在构造LoggingEvent的时候，会用MessageFormat类来格式化msg，将{}替换成具体的参数值。
示例如下：
`logger.info("{},it's OK.","Hi");`
则输出结果如下：
`Hi,it's OK.`
以上代码是在java文件中调用logger类来进行输出的时候可以用到，不是xml。
- prudent
		当`<prudent>true</prudent>  `这条语句加入到appender后，那么logback就支持多个jvm操作同一个日志文件，虽然不建议，但是提供该方面技术的支持，也要注意以下几点
1. 如果多JVM同时操作同一个文件，则会出现日志不回滚、打出的日志串掉的场景。
2. 如果按小时来回滚，并且一个小时内并没有业务日志输出，那么这个小时的日志文件是不会生成的，会跳过这个小时的日志文件的生成。`<maxHistory>`也是同样的，如果隔一段时间没有输出日志，前面过期的日志不会被删除，只有再重新打印日志的时候，会触发删除过期日志的操作。
3. 官方给的说明如下：如果使用prudent模式，FileAppender将安全的写入到指定文件，即使存在运行在不同机器上的、其他JVM中运行的其他FileAppender实例。Prudent模式更依赖于排他文件锁，经验表明加了文件锁后，写日志的开支是正常的3倍以上。当prudent模式关闭时，每秒logging event的吞吐量为100,000，当prudent模式开启时，大约为每秒33,000。
4. 如果日志打印较多，则可能会出现将硬盘撑爆的情况，还是建议使用FixedWindowRollingPolicy回滚策略，这种策略固定了日志文件大小，超出则回滚。业务上出现了多个jvm同时操作同一个日志文件，仍建议每个jvm只操作一个日志文件。

- 自定义Pattern
写一个转换器类，继承ClassicConvert
	```
public class IpConvert extends ClassicConverter {  
  
    @Override  
    public String convert(ILoggingEvent event) {  
        return "10.10.10.10";  
    }  
} 
	```
再在xml中添加以下配置
`<conversionRule conversionWord="ip" converterClass="com.cj.log.IpConvert" />`

	自定义ip转换符
`<Pattern>%d{yyyy-MM-dd HH:mm:ss.SSS}%ip [%thread] %-5level %logger{36} -% msg%n</Pattern>`
这样的效果是有关ip的字段会被自动替换为10.10.10.10
这个功能可以用于一些特殊日志的打印，自定义了日志输出格式，方便差错或其他特殊的要求。

- AsyncAppender
异步记录日志，AsyncAppender并不处理日志，只是将日志缓冲到一个BlockingQueue里面去，并在内部创建一个工作线程从队列头部获取日志，之后将获取的日志循环记录到附加的其他appender上去，从而达到不阻塞主线程的效果。因此AsynAppender仅仅充当事件转发器，必须引用另一个appender来做事。

```
<!-- 异步输出 -->  
<appender name ="ASYNC" class= "ch.qos.logback.classic.AsyncAppender">  
<!-- 不丢失日志.默认的,如果队列的80%已满,则会丢弃TRACT、DEBUG、INFO级别的日志 -->  
<discardingThreshold >0</discardingThreshold>  
<!-- 更改默认的队列的深度,该值会影响性能.默认值为256 -->  
<queueSize>512</queueSize>  
<!-- 添加附加的appender,最多只能添加一个 -->  
<appender-ref ref ="FILE"/>  
</appender>  
```

`<appender-ref ref ="FILE"/>  `此条语句就是导向另一个实际进行输出的appender

在使用AsyncAppender的时候，有些选项还是要注意的。由于使用了BlockingQueue来缓存日志，因此就会出现队列满的情况。正如上面原理中所说的，在这种情况下，AsyncAppender会做出一些处理：默认情况下，如果队列80%已满，AsyncAppender将丢弃TRACE、DEBUG和INFO级别的event，从这点就可以看出，该策略有一个惊人的对event丢失的代价性能的影响。另外其他的一些选项信息，也会对性能产生影响，下面列出常用的几个属性配置信息：

|属性名|类型|描述|
|--|--|--|
|queueSize|int|BlockingQueue的最大容量，默认情况下，大小为256|
|discardingThreshold|int|默认情况下，当BlockingQueue还有20%容量，他将丢弃TRACE、DEBUG和INFO级别的event，只保留WARN和ERROR级别的event。为了保持所有的events，设置该值为0。|
|includeCallerData|boolean|提取调用者数据的代价是相当昂贵的。为了提升性能，默认情况下，当event被加入到queue时，event关联的调用者数据不会被提取。默认情况下，只有"cheap"的数据，如线程名。|

####日志总结
SpringBoot采用的log为logback，作为log4j的加强版，添加了一些额外的功能来方便调试，有了logback的强大功能，可以延时输出log，自定义格式化输出内容等等，是非常强大的日志系统。