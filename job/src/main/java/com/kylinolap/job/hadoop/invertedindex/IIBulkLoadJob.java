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

package com.kylinolap.job.hadoop.invertedindex;

import org.apache.commons.cli.Options;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.permission.FsPermission;
import org.apache.hadoop.hbase.mapreduce.LoadIncrementalHFiles;
import org.apache.hadoop.util.ToolRunner;

import com.kylinolap.common.KylinConfig;
import com.kylinolap.common.util.HadoopUtil;
import com.kylinolap.cube.CubeInstance;
import com.kylinolap.cube.CubeManager;
import com.kylinolap.cube.CubeSegment;
import com.kylinolap.cube.CubeSegmentStatusEnum;
import com.kylinolap.job.hadoop.AbstractHadoopJob;
import com.kylinolap.metadata.model.invertedindex.InvertedIndexDesc;

/**
 * @author ysong1
 * 
 */
public class IIBulkLoadJob extends AbstractHadoopJob {

    @Override
    public int run(String[] args) throws Exception {
        Options options = new Options();

        try {
            options.addOption(OPTION_INPUT_PATH);
            options.addOption(OPTION_HTABLE_NAME);
            options.addOption(OPTION_CUBE_NAME);
            parseOptions(options, args);

            String tableName = getOptionValue(OPTION_HTABLE_NAME);
            String input = getOptionValue(OPTION_INPUT_PATH);
            String cubeName = getOptionValue(OPTION_CUBE_NAME);

            FileSystem fs = FileSystem.get(getConf());
            FsPermission permission = new FsPermission((short) 0777);
            fs.setPermission(new Path(input, InvertedIndexDesc.HBASE_FAMILY), permission);

            int hbaseExitCode = ToolRunner.run(new LoadIncrementalHFiles(getConf()), new String[] { input, tableName });

            CubeManager mgr = CubeManager.getInstance(KylinConfig.getInstanceFromEnv());
            CubeInstance cube = mgr.getCube(cubeName);
            CubeSegment seg = cube.getFirstSegment();
            seg.setStorageLocationIdentifier(tableName);
            seg.setStatus(CubeSegmentStatusEnum.READY);
            mgr.updateCube(cube);

            return hbaseExitCode;

        } catch (Exception e) {
            printUsage(options);
            e.printStackTrace(System.err);
            return 2;
        }
    }

    public static void main(String[] args) throws Exception {
        IIBulkLoadJob job = new IIBulkLoadJob();
        job.setConf(HadoopUtil.newHBaseConfiguration(KylinConfig.getInstanceFromEnv().getStorageUrl()));
        int exitCode = ToolRunner.run(job, args);
        System.exit(exitCode);
    }
}
