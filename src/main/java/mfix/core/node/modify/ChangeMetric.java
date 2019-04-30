/**
 * Copyright (C) SEI, PKU, PRC. - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited Proprietary and Confidential.
 * Written by Jiajun Jiang<jiajun.jiang@pku.edu.cn>.
 */

package mfix.core.node.modify;

/**
 * @author: Jiajun
 * @date: 2019-04-30
 */
public class ChangeMetric {

    private int _upd;
    private int _ins;
    private int _del;
    private int _mod;
    private int _all;
    private int _cluster;
    private String _file;

    public ChangeMetric(String file, int modnum, int upd, int ins, int del, int cluster) {
        _file = file;
        _upd = upd;
        _ins = ins;
        _del = del;
        _mod = modnum;
        _cluster = cluster;
        _all = upd + ins + del;
    }

    public int getModificationNumber() {
        return _mod;
    }

    public int getChangeNumber() {
        return _all;
    }

    public int negIns() {
        return - _ins;
    }

    public int negUpd() {
        return - _upd;
    }

    public int geDel() {
        return - _del;
    }

    public int getIns() {
        return _ins;
    }

    public int getUpd() {
        return _upd;
    }

    public int getDel() {
        return _del;
    }

    public int negCluster() {
        return - _cluster;
    }

    public int getCluster() {
        return _cluster;
    }

    public String getFile() {
        return _file;
    }

    @Override
    public String toString() {
        return "<" + _mod + ", " + _all + ", " + _upd + ", " + _ins + ", " + _del + ", " + _cluster + ">";
    }
}