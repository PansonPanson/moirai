/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package top.panson.server.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * @方法描述：数据库配置信息对象
 */
@Configuration
public class DataBaseConfiguration {

    @Bean
    @ConditionalOnMissingBean(value = DataBaseProperties.class)
    public DataBaseProperties dataBaseProperties(@Value("${hippo4j.database.dialect:h2}") String dialect,
                                                 @Value("${hippo4j.database.init_script:sql-script/h2/schema.sql}") String initScript,
                                                 @Value("${hippo4j.database.init_enable:false}") Boolean initEnable) {
        DataBaseProperties dataSourceProperties = new DataBaseProperties();
        dataSourceProperties.setDialect(dialect);
        dataSourceProperties.setInitScript(initScript);
        dataSourceProperties.setInitEnable(initEnable);
        return dataSourceProperties;
    }
}
