import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { SearchBox } from '@/features/search/SearchBox';
import { WatchlistTable } from '@/features/watchlist/WatchlistTable';
import { Search, Star } from 'lucide-react';

export function HomePage() {
  return (
    <div className="grid grid-cols-1 gap-6 lg:grid-cols-5">
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
    </div>
  );
}
