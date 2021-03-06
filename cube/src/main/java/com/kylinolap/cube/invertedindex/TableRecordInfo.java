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

package com.kylinolap.cube.invertedindex;

import java.io.IOException;

import com.kylinolap.cube.CubeSegment;
import com.kylinolap.dict.Dictionary;
import com.kylinolap.dict.DictionaryManager;
import com.kylinolap.metadata.model.cube.TblColRef;
import com.kylinolap.metadata.model.invertedindex.InvertedIndexDesc;
import com.kylinolap.metadata.model.schema.ColumnDesc;
import com.kylinolap.metadata.model.schema.TableDesc;

/**
 * @author yangli9
 * 
 */
public class TableRecordInfo {

    final CubeSegment seg;
    final InvertedIndexDesc desc;
    final TableDesc tableDesc;

    final int nColumns;
    final String[] colNames;
    final Dictionary<?>[] dictionaries;

    final int byteFormLen;
    final int[] offsets;

    public TableRecordInfo(CubeSegment cubeSeg) throws IOException {

        seg = cubeSeg;
        desc = seg.getCubeInstance().getInvertedIndexDesc();
        tableDesc = desc.getFactTableDesc();

        nColumns = tableDesc.getColumnCount();
        colNames = new String[nColumns];
        dictionaries = new Dictionary<?>[nColumns];

        DictionaryManager dictMgr = DictionaryManager.getInstance(desc.getConfig());
        for (ColumnDesc col : tableDesc.getColumns()) {
            int i = col.getZeroBasedIndex();
            colNames[i] = col.getName();
            String dictPath = seg.getDictResPath(new TblColRef(col));
            dictionaries[i] = dictMgr.getDictionary(dictPath);
        }

        int pos = 0;
        offsets = new int[nColumns];
        for (int i = 0; i < nColumns; i++) {
            int size = dictionaries[i].getSizeOfId();
            offsets[i] = pos;
            pos += size;
        }
        byteFormLen = pos;
    }

    public long calculateTimePartition(long ts) {
        return ts - ts % getTimestampGranularity();
    }

    public InvertedIndexDesc getDescriptor() {
        return desc;
    }

    public ColumnDesc[] getColumns() {
        return tableDesc.getColumns();
    }

    public int getColumnCount() {
        return nColumns;
    }

    @SuppressWarnings("unchecked")
    public Dictionary<String> dict(int col) {
        // yes, all dictionaries are string based
        return (Dictionary<String>) dictionaries[col];
    }

    public int offset(int col) {
        return offsets[col];
    }

    public int length(int col) {
        return dictionaries[col].getSizeOfId();
    }

    public int getTimestampColumn() {
        return desc.getTimestampColumn();
    }

    public int getTimestampGranularity() {
        return desc.getTimestampGranularity();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((seg == null) ? 0 : seg.hashCode());
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        TableRecordInfo other = (TableRecordInfo) obj;
        if (seg == null) {
            if (other.seg != null)
                return false;
        } else if (!seg.equals(other.seg))
            return false;
        return true;
    }

}
