<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:if test="${ not empty storeList }">
<script> app.view.stores = {<c:forEach var="store" items="${ storeList }">"${ store.name }": "${ store.displayName }",</c:forEach>}; </script>
<script src="${ pageContext.request.contextPath }/resources/marketplace/js/views/descriptions/create.js"></script>
<c:if test="${ not empty form_data }">
<script>
  app.view.descriptionCreateForm
  <c:forEach var="field" items="${ form_data }">
    .addInitialValue("${ field.key }", "${ field.value }")
  </c:forEach>
    .addErrorMessage("${ form_error.fieldName }", "${ form_error.fieldError }")
</script>
</c:if>
</c:if>