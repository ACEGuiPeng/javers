package org.javers.core.diff.appenders.levenshtein;

import org.javers.common.validation.Validate;
import org.javers.core.diff.EqualsFunction;
import org.javers.core.diff.appenders.CorePropertyChangeAppender;
import org.javers.core.diff.changetype.container.ContainerElementChange;
import org.javers.core.diff.changetype.container.ListChange;
import org.javers.core.metamodel.object.*;
import org.javers.core.metamodel.property.Property;
import org.javers.core.metamodel.type.*;

import java.util.List;

/**
 * @author kornel kielczewski
 */
public class LevenshteinListChangeAppender extends CorePropertyChangeAppender<ListChange> {

    private final TypeMapper typeMapper;

    LevenshteinListChangeAppender(TypeMapper typeMapper) {
        Validate.argumentsAreNotNull(typeMapper);
        this.typeMapper = typeMapper;
    }

    @Override
    public boolean supports(JaversType propertyType) {
        return propertyType instanceof ListType;
    }

    @Override
    public ListChange calculateChanges(Object leftValue, Object rightValue, GlobalId affectedId, JaversProperty property) {
        CollectionType listType = property.getType();
        JaversType itemType = typeMapper.getJaversType(listType.getItemType());

        final List leftList =  (List) leftValue;
        final List rightList = (List) rightValue;

        EqualsFunction equalsFunction = itemType::equals;
        Backtrack backtrack = new Backtrack(equalsFunction);
        StepsToChanges stepsToChanges = new StepsToChanges(equalsFunction);

        final BacktrackSteps[][] steps = backtrack.evaluateSteps(leftList, rightList);
        final List<ContainerElementChange> changes = stepsToChanges.convert(steps, leftList, rightList);

        ListChange result = getListChange(affectedId, property, changes);
        if (result != null) {
            renderNotParametrizedWarningIfNeeded(listType.getItemType(), "item", "List", property);
        }
        return result;
    }

    private ListChange getListChange(final GlobalId affectedCdoId, final Property property,
                                     final List<ContainerElementChange> changes) {
        final ListChange result;

        if (changes.size() == 0) {
            result = null;
        } else {
            result = new ListChange(affectedCdoId, property.getName(), changes);
        }
        return result;
    }
}
