import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { SearchBox } from '@/features/search/SearchBox';
import { IndexTicker } from '@/features/marketOverview/IndexTicker';
import { MarketBreadthBar } from '@/features/marketOverview/MarketBreadthBar';
import { SectorMoneyFlow } from '@/features/marketOverview/SectorMoneyFlow';
import { SectorTreemap } from '@/features/marketOverview/SectorTreemap';
import { WatchlistTable } from '@/features/watchlist/WatchlistTable';
import { LayoutGrid, Search, Star, Waves } from 'lucide-react';

export function HomePage() {
  return (
    <div className="flex flex-col gap-8">
      <section className="flex flex-col gap-3">
        <div className="flex items-baseline gap-2">
          <h2 className="display text-muted-foreground text-xs">Chỉ số thị trường</h2>
          <span className="bg-accent-soft text-accent rounded-full px-2 py-0.5 text-[10px] font-bold uppercase">
            Mới
          </span>
        </div>
        <IndexTicker />
      </section>

      <section className="flex flex-col gap-3">
        <div className="flex items-baseline gap-2">
          <h2 className="display text-muted-foreground text-xs">Độ rộng thị trường</h2>
          <span className="bg-accent-soft text-accent rounded-full px-2 py-0.5 text-[10px] font-bold uppercase">
            Mới
          </span>
        </div>
        <MarketBreadthBar />
      </section>

      <section className="grid grid-cols-1 gap-4 lg:grid-cols-3">
        <Card className="lg:col-span-2">
          <CardHeader>
            <CardTitle className="flex items-center gap-2.5">
              <span className="bg-accent-soft text-accent rounded-md p-1.5">
                <LayoutGrid className="h-4 w-4" />
              </span>
              Bản đồ ngành theo vốn hóa
            </CardTitle>
            <CardDescription>Diện tích = tỷ trọng vốn hóa · Màu = % thay đổi trong phiên</CardDescription>
          </CardHeader>
          <CardContent>
            <SectorTreemap />
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2.5">
              <span className="bg-accent-soft text-accent rounded-md p-1.5">
                <Waves className="h-4 w-4" />
              </span>
              Dòng tiền theo ngành
            </CardTitle>
            <CardDescription>Giá trị khớp lệnh ròng, tỷ đồng</CardDescription>
          </CardHeader>
          <CardContent>
            <SectorMoneyFlow />
          </CardContent>
        </Card>
      </section>

      <section className="grid grid-cols-1 gap-6 lg:grid-cols-5">
        <Card className="lg:col-span-2">
          <CardHeader>
            <CardTitle className="flex items-center gap-2.5">
              <span className="bg-primary/10 text-primary rounded-md p-1.5">
                <Search className="h-4 w-4" />
              </span>
              Tìm mã cổ phiếu
            </CardTitle>
            <CardDescription>Tìm theo mã hoặc tên công ty để thêm vào watchlist</CardDescription>
          </CardHeader>
          <CardContent>
            <SearchBox />
          </CardContent>
        </Card>

        <Card className="lg:col-span-3">
          <CardHeader>
            <CardTitle className="flex items-center gap-2.5">
              <span className="rounded-md bg-amber-500/10 p-1.5 text-amber-600">
                <Star className="h-4 w-4" />
              </span>
              Watchlist
            </CardTitle>
            <CardDescription>Giá cập nhật real-time mỗi 3 giây</CardDescription>
          </CardHeader>
          <CardContent>
            <WatchlistTable />
          </CardContent>
        </Card>
      </section>
    </div>
  );
}
