package oracleconnector.impl;


import com.mendix.core.Core;
import com.mendix.systemwideinterfaces.core.IContext;
import com.mendix.systemwideinterfaces.core.IMendixObject;

import oracleconnector.interfaces.ObjectInstantiator;

class ObjectInstantiatorImpl implements ObjectInstantiator {

    @Override
    public IMendixObject instantiate(final IContext context, final String entityName) {
        return Core.instantiate(context, entityName);
    }
}