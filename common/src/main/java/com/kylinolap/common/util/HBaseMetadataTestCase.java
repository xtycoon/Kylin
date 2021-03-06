/*
 * Copyright 2013-2014 eBay Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kylinolap.common.util;

import com.kylinolap.common.KylinConfig;
import com.kylinolap.common.persistence.ResourceStore;
import com.kylinolap.common.persistence.ResourceTool;

/**
 * @author ysong1
 */
public class HBaseMetadataTestCase extends AbstractKylinTestCase {

    public static void staticCreateTestMetadata() {

        KylinConfig.destoryInstance();

        if (System.getProperty(KylinConfig.KYLIN_CONF) == null && System.getenv(KylinConfig.KYLIN_CONF) == null)
            System.setProperty(KylinConfig.KYLIN_CONF, TEST_DATA_FOLDER);

    }
    
    public static void staticCleanupTestMetadata() {
        System.clearProperty(KylinConfig.KYLIN_CONF);
        KylinConfig.destoryInstance();
    }

    @Override
    public void createTestMetadata() {
        staticCreateTestMetadata();
    }

    @Override
    public void cleanupTestMetadata() {
        staticCleanupTestMetadata();
    }

    @Override
    public KylinConfig getTestConfig() {
        return KylinConfig.getInstanceFromEnv();
    }

    public void installMetadataToHBase() throws Exception {
        // install metadata to hbase
        ResourceTool.reset(KylinConfig.getInstanceFromEnv());
        ResourceTool.copy(KylinConfig.createInstanceFromUri(TEST_DATA_FOLDER), KylinConfig.getInstanceFromEnv());
    }

    protected ResourceStore getStore() {
        return ResourceStore.getStore(KylinConfig.getInstanceFromEnv());
    }
}
