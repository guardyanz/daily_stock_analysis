package id.ihsg.stockanalysis

import id.ihsg.stockanalysis.data.IhsgSymbol
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class IhsgSymbolTest {
    @Test
    fun normalizeAddsJakartaSuffix() {
        assertEquals("BBCA.JK", IhsgSymbol.normalize("bbca"))
        assertEquals("TLKM.JK", IhsgSymbol.normalize(" TLKM.JK "))
    }

    @Test
    fun validationAcceptsOnlyIdxSymbols() {
        assertTrue(IhsgSymbol.isValid("ASII.JK"))
        assertFalse(IhsgSymbol.isValid("AAPL"))
        assertFalse(IhsgSymbol.isValid("600519.SS"))
    }
}
