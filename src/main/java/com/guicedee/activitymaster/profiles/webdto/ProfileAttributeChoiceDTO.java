package com.guicedee.activitymaster.profiles.webdto;

/**
 * One selectable value for a list-valued profile attribute.
 *
 * @param value the child classification name, stored as the attribute value
 * @param label the classification description, shown to the user
 */
public record ProfileAttributeChoiceDTO(String value, String label)
{
}
