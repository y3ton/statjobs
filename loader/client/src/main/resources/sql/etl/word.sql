SELECT  word, count(*) FROM
    (SELECT regexp_split_to_table(lower(data->>'description'), E'\\W') word
        FROM public.t_hh_raw_vacancies
        WHERE sequence_num = 1520571008
        AND   lower(data->>'description') like '%java%' AND lower(data->>'description') not like '%javascript%'
    ) words
WHERE char_length(word) > 1
AND word not in ('li', 'ul', 'strong', 'em', 'br', 'quot', 'in', 'the', 'of', 'to', 'and', 'for', 'it', 'you', 'we', 'is', 'are', 'and', 'our', 'on', 'as', 'be', 'or')
AND substring(word, '[a-z1-9]*') <> ''
GROUP BY word
ORDER BY count(*) DESC