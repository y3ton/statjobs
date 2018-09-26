redis-cli -h redis1.0001.usw2.cache.amazonaws.com keys P* | \
    sed -e "s/^/get\t/" | \
    redis-cli -h redis1.0001.usw2.cache.amazonaws.com | \
    sed -e "s/^/LPUSH\tl1\t\'/" | \
    sed -e "s/$/\'/" | \    
    redis-cli -h redis1.0001.usw2.cache.amazonaws.com