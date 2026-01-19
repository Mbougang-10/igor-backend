import { useTranslations } from 'next-intl';
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from '@/components/ui/card';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Button } from '@/components/ui/button';

export default function LoginPage() {
  const t = useTranslations('login');

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50 dark:bg-gray-900 px-4">
      <Card className="w-full max-w-md">
        <CardHeader className="text-center">
          <CardTitle className="text-2xl font-bold">{t('title')}</CardTitle>
          <CardDescription className="mt-2">
            {t('hasAccount')}{' '}
            <a href="#" className="text-blue-600 hover:underline">
              {t('switchToSignIn')}
            </a>
          </CardDescription>
        </CardHeader>

        <CardContent>
          <Tabs defaultValue="signin" className="w-full">
            <TabsList className="grid w-full grid-cols-2">
              <TabsTrigger value="signin">{t('signInTab')}</TabsTrigger>
              <TabsTrigger value="create">{t('createOrgTab')}</TabsTrigger>
            </TabsList>

            {/* Sign In Tab */}
            <TabsContent value="signin" className="space-y-4 mt-4">
              <div>
                <Label htmlFor="email-signin">{t('email')}</Label>
                <Input id="email-signin" type="email" placeholder="adam@example.com" required />
              </div>
              <div>
                <Label htmlFor="password-signin">{t('password')}</Label>
                <Input id="password-signin" type="password" required />
              </div>
              <Button className="w-full">{t('signInButton')}</Button>
              <p className="text-sm text-center text-gray-600 dark:text-gray-400">
                {t('noAccount')}{' '}
                <a href="#" className="text-blue-600 hover:underline">
                  {t('switchToCreate')}
                </a>
              </p>
            </TabsContent>

            {/* Create Organization Tab */}
            <TabsContent value="create" className="space-y-4 mt-4">
              <div>
                <Label htmlFor="org-name">{t('orgName')}</Label>
                <Input id="org-name" placeholder="My Awesome Community" required />
              </div>
              <div>
                <Label htmlFor="email-create">{t('email')}</Label>
                <Input id="email-create" type="email" placeholder="adam@example.com" required />
              </div>
              <div>
                <Label htmlFor="password-create">{t('password')}</Label>
                <Input id="password-create" type="password" required />
              </div>
              <div>
                <Label htmlFor="logo">{t('orgLogo')}</Label>
                <Input id="logo" type="file" accept="image/*" />
              </div>
              <Button className="w-full">{t('createButton')}</Button>
            </TabsContent>
          </Tabs>
        </CardContent>
      </Card>
    </div>
  );
}