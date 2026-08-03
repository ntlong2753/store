import urllib.request, re, json

def get_image(query):
    try:
        q = urllib.parse.quote(query + ' product image')
        req = urllib.request.Request(f'https://html.duckduckgo.com/html/?q={q}', headers={'User-Agent': 'Mozilla/5.0'})
        html = urllib.request.urlopen(req).read().decode('utf-8')
        links = re.findall(r'src=\"(//external-content\.duckduckgo\.com/iu/\?u=[^\"]+)\"', html)
        if links:
            return 'https:' + links[0]
    except Exception as e:
        print("error", e)
    return ''

items = ['Intel Core i9 14900K box', 'Intel Core i5 13400F box', 'AMD Ryzen 9 7950X box', 'AMD Ryzen 5 7600X box', 'Intel Core i3 12100F box', 'Corsair Vengeance RGB DDR5 32GB', 'G.Skill Trident Z5 RGB 32GB', 'Kingston FURY Beast 16GB DDR4', 'Adata XPG Lancer 32GB', 'TeamGroup T-Force Delta RGB 32GB', 'ASUS ROG Strix RTX 4090', 'MSI RTX 4070 Ti SUPER', 'Gigabyte RTX 4060 Eagle OC', 'Sapphire RX 7800 XT', 'ASUS TUF RX 7600', 'Samsung 990 PRO 2TB', 'WD Black SN850X 1TB', 'Crucial P3 Plus 1TB', 'Seagate BarraCuda 2TB', 'Samsung 870 EVO 1TB']

print("Starting...")
for item in items:
    print(item + '::::' + get_image(item))
