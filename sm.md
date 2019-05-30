

# springboot-elasticsearch

#### һ����Ŀ����
Springboot2.1.1+elasticsearch6.5.3�����ҵ������ƽ̨��֧��PB�����ݣ���Ҫelasticsearch�ֲ�ʽ���𣩣�Ŀǰ�Ѿ�֧�����ķִʣ������ؼ��ʸ�������������ﵽ�����鷳����Star��лл��

���� Springboot2.1+Solr7.5 ����������棬�Ѿ�֧���ĵ����������ݿ����������ķִʵȡ� [https://gitee.com/11230595/springboot-solr](https://gitee.com/11230595/springboot-solr)

#### ��������ܹ�
1. Springboot2.1.1
2. elasticsearch6.5.3
3. spring-boot-starter-data-elasticsearch
4. analysis-ik 6.5.3

#### �������ý̳�

1. elasticsearch6.5.3  <br/>
    - ���� <br/>
    https://www.elastic.co/cn/downloads/elasticsearch <br/>
    - ����<br/>
    ��ѹ�󣬴� ```config/elasticsearch.yml```���������������ý����޸� <br/>
        - ```cluster.name```��Ⱥ���ƣ������д������ʹ��Ĭ�ϵġ�my-application����ע�⣬����Java����elasticsearchʱ����Ҫ�����á�
        - ```network.host```����˲����ô�����������޷����ӵ�ǰelasticsearch������Ϊ����0.0.0.0�����κ�IP���ɷ��ʣ�
        - ���� <br/>
        Mac/Linux������ ```bin/elasticsearch```<br/>
        Windows������ ```bin\elasticsearch.bat```
2. analysis-ik 6.5.3 <br/>
    - ��װִ����� <br/>
    ```bin/elasticsearch-plugin install https://github.com/medcl/elasticsearch-analysis-ik/releases/download/v6.5.3/elasticsearch-analysis-ik-6.5.3.zip```
3. essearch <br/>
    ��׼springboot��Ŀ������IDE���м��ɡ�
#### �ġ�essearch����˵��

1. �޸�```application.properties->spring.data.elasticsearch.cluster-nodes```  elasticsearch��ַ
2. �޸�```application.properties->spring.data.elasticsearch.cluster-name``` ��Ⱥ���ƣ����������õ����Ӧ
3. ���ֲ�����ʽ����ο���``` /src/test/java/com/zhou/essearch/EssearchApplicationTests.java ```������
4. �˿ڣ��ĸ����������������������

#### �塢�����ӿڷ������ݽ�ͼ
- ��ҳ�����ӿ� <br>
![image](https://images.gitee.com/uploads/images/2018/1218/110942_55dcc26e_499215.png) <br>
- ��ͨ�����ӿ� <br>
![image](https://images.gitee.com/uploads/images/2018/1214/223726_f913dbf0_499215.png)

#### ��������
1. ���ݿ�����ͬ���������а�װ�����
2. ����mq���ӿڷ�ʽͬ�����ݣ���鿴��Ŀ�е�save�ӿ�ģ�顣

#### �ߡ�QQȺ��83402555

#### �ˡ���ע���ںţ����ں����а�װes�Ĳ���ͺܶ�ʵ�����£�
![image](https://images.gitee.com/uploads/images/2018/1210/122022_148f50d8_499215.jpeg)

