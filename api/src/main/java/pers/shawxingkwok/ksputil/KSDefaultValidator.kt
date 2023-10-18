package pers.shawxingkwok.ksputil

import com.google.devtools.ksp.symbol.*
import com.google.devtools.ksp.visitor.KSDefaultVisitor
import com.google.devtools.ksp.visitor.KSValidateVisitor

/**
 * A simplified and optimized [KSValidateVisitor].
 */
public open class KSDefaultValidator : KSDefaultVisitor<Unit, Boolean>() {
    private fun KSNode.accept() = accept(this@KSDefaultValidator, Unit)
    private fun Sequence<KSNode>.allAccept() = all { it.accept() }
    private fun List<KSNode>.allAccept() = all { it.accept() }

    protected open fun validateType(type: KSType): Boolean =
        !type.isError
        && type.arguments.all { it.type!!.accept() }

    override fun defaultHandler(node: KSNode, data: Unit): Boolean = true

    override fun visitDeclaration(declaration: KSDeclaration, data: Unit): Boolean =
        declaration.typeParameters.allAccept()
        && visitAnnotated(declaration, data)

    override fun visitDeclarationContainer(declarationContainer: KSDeclarationContainer, data: Unit): Boolean =
        declarationContainer.declarations.allAccept()

    override fun visitTypeParameter(typeParameter: KSTypeParameter, data: Unit): Boolean =
        typeParameter.bounds.allAccept()

    override fun visitAnnotated(annotated: KSAnnotated, data: Unit): Boolean =
        annotated.annotations.allAccept()

    override fun visitAnnotation(annotation: KSAnnotation, data: Unit): Boolean =
        annotation.annotationType.accept()
        && annotation.arguments.allAccept()

    override fun visitTypeReference(typeReference: KSTypeReference, data: Unit): Boolean =
        validateType(typeReference.resolve())

    override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit): Boolean =
        !classDeclaration.asStarProjectedType().isError
        && classDeclaration.superTypes.allAccept()
        && visitDeclaration(classDeclaration, data)
        && visitDeclarationContainer(classDeclaration, data)

    override fun visitFunctionDeclaration(function: KSFunctionDeclaration, data: Unit): Boolean =
        (function.returnType?.accept() ?: true)
        && function.parameters.allAccept()
        && visitDeclaration(function, data)

    override fun visitPropertyDeclaration(property: KSPropertyDeclaration, data: Unit): Boolean =
        property.type.accept() && visitDeclaration(property, Unit)

    override fun visitValueArgument(valueArgument: KSValueArgument, data: Unit): Boolean {
        fun visitValue(value: Any?): Boolean =
            when (value) {
                is KSType -> validateType(value)
                is KSAnnotation -> visitAnnotation(value, data)
                is List<*> -> value.all(::visitValue)
                else -> true
            }

        return visitValue(valueArgument.value)
    }

    override fun visitValueParameter(valueParameter: KSValueParameter, data: Unit): Boolean =
        valueParameter.type.accept()

    // This is the corrected part.
    override fun visitTypeAlias(typeAlias: KSTypeAlias, data: Unit): Boolean =
        visitTypeReference(typeAlias.type, data)
        && super.visitTypeAlias(typeAlias, data)
}