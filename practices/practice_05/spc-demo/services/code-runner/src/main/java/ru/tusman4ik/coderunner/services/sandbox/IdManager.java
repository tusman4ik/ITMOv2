package ru.tusman4ik.coderunner.services.sandbox;

import lombok.extern.slf4j.Slf4j;

import java.util.BitSet;

@Slf4j
public final class IdManager {
    private final int RATIONAL_LIMIT_ID = 100;

    private final BitSet bitSet = new BitSet(Short.MAX_VALUE); // even 32k is bigger, than enough
    private final Object SYNC = new Object();

    public int acquire() {
        synchronized (SYNC) {
            int id = bitSet.nextClearBit(0);
            if (id > RATIONAL_LIMIT_ID) {
                log.warn("Rational limit of sandbox ids exceeded! Probably leak! Current id = {}", id);
            }
            bitSet.set(id);
            return id;
        }
    }

    public void release(int id){
        synchronized (SYNC){
            bitSet.clear(id);
        }
    }

}
