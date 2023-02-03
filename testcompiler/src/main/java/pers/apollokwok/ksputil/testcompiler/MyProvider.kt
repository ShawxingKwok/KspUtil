package pers.apollokwok.ksputil.testcompiler

import pers.apollokwok.ksputil.KspProvider
import pers.apollokwok.ksputil.testcompiler.test.TestProcessor

internal class MyProvider : KspProvider(::TestProcessor, { MyProcessor })