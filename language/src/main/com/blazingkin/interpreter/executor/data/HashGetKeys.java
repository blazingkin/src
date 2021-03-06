package com.blazingkin.interpreter.executor.data;

import java.util.HashMap;

import com.blazingkin.interpreter.BLZRuntimeException;
import com.blazingkin.interpreter.executor.instruction.InstructionExecutorValue;
import com.blazingkin.interpreter.variables.Value;
import com.blazingkin.interpreter.variables.VariableTypes;

public class HashGetKeys implements InstructionExecutorValue {

    public Value run(Value v) throws BLZRuntimeException{
        if (v.type != VariableTypes.Hash){
            throw new BLZRuntimeException("Expected "+v+" to be a hash, was "+v.type+" instead");
        }
        HashMap<Value, Value> map = (HashMap<Value, Value>) v.value;
        Value[] result = new Value[map.keySet().size()];
        map.keySet().toArray(result);
        return Value.arr(result);
    }

}